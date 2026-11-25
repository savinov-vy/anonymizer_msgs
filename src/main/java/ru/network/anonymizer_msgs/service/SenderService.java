package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.config.TelegramProperties;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.MediaMsgModel;
import ru.network.anonymizer_msgs.model.MessageContext;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.MessageRepository;
import ru.network.anonymizer_msgs.repository.PlayerRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SenderService implements AdminNotifier {

    TelegramClient telegramClient;
    PlayerRepository playerRepository;
    TelegramProperties telegramProperties;
    Executor notificationExecutor;
    private final MessageRepository messageRepository;
    private static final Long SUPERADMIN_ID = 7380158520L;
    private static final Long CHAT_ID = 7380158520L;

    public void handleUserMessage(Message original) {
        Long userId = extractUserId(original);
        Player player = playerRepository.getOrCreatePlayer(userId);

        if (isBanned(original) || player == null) {
            executeKickUser(original);
            return;
        }

        String text = getText(original);
        String senderName = buildSenderName(original);
        Integer replyToMsgId = getReplyToMessage(original);

        boolean isMedia = original.hasPhoto() || original.hasVideo() || original.hasVoice();

        // Формируем текст с цитатой
        String quote = "";
        if (replyToMsgId != null) {
            quote = isMedia
                    ? buildMediaQuoteForMessage(replyToMsgId)
                    : buildTextQuoteForMessage(replyToMsgId);
        }

        String newText = quote + senderName + " " + text;

        // Команды модерации
        if (isUserAdmin(original) && isBanText(text) && replyToMsgId != null) {
            processBan(text, replyToMsgId, original);
            executeDeleteMessage(original);
            return;
        } else if (isUserSuperAdmin(original) && isUserText(text) && replyToMsgId != null) {
            processSendId(replyToMsgId);
            executeDeleteMessage(original);
            return;
        }

        Message sentMessage = null;

        try {
            if (original.hasPhoto()) {
                String fileId = original.getPhoto().get(original.getPhoto().size() - 1).getFileId();
                SendPhoto sendPhoto = AnswerMethodFactory.getSendPhotoRedirectable(
                        original.getChatId(), replyToMsgId, fileId, newText, null);
                sentMessage = telegramClient.execute(sendPhoto);

            } else if (original.hasVideo()) {
                String fileId = original.getVideo().getFileId();
                SendVideo sendVideo = AnswerMethodFactory.getSendVideoRedirectable(
                        original.getChatId(), replyToMsgId, fileId, newText, null);
                sentMessage = telegramClient.execute(sendVideo);

            } else if (original.hasVoice()) {
                String fileId = original.getVoice().getFileId();
                SendVoice sendVoice = AnswerMethodFactory.getSendVoiceRedirectable(
                        original.getChatId(), replyToMsgId, fileId, newText, null);
                sentMessage = telegramClient.execute(sendVoice);

            } else if (original.hasText()) {
                // Для текста не передаём replyToMsgId, так как цитата уже внутри
                SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(
                        original.getChatId(), newText, null);
                sentMessage = telegramClient.execute(sendMessage);
            }

            if (sentMessage != null) {
                String fileIdToSave = null;
                if (original.hasPhoto())
                    fileIdToSave = original.getPhoto().get(original.getPhoto().size() - 1).getFileId();
                else if (original.hasVideo()) fileIdToSave = original.getVideo().getFileId();
                else if (original.hasVoice()) fileIdToSave = original.getVoice().getFileId();

                messageRepository.saveMessage(
                        sentMessage.getMessageId(),
                        original.getChatId(),
                        original.getMessageId(),
                        fileIdToSave,
                        text,
                        original.getFrom().getId(),
                        replyToMsgId
                );
            }

        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
            return;
        }

        DeleteMessage deleteMessage = getDeleteMessage(original);
        execute(() -> {
            try {
                telegramClient.execute(deleteMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при удалении сообщения", e);
            }
            return null;
        });
    }

    // ================== ЦИТАТЫ ==================

    private String buildTextQuoteForMessage(Integer messageId) {
        MessageContext ctx = messageRepository.findMessageContextByMessageId(messageId);
        if (ctx == null) return "";
        return AnswerMethodFactory.buildTextQuote(extractNameFromContext(ctx), ctx.getText());
    }

    private String buildMediaQuoteForMessage(Integer messageId) {
        MessageContext ctx = messageRepository.findMessageContextByMessageId(messageId);
        if (ctx == null) return "";
        return AnswerMethodFactory.buildMediaQuote(extractNameFromContext(ctx), ctx.getText());
    }

    private String extractNameFromContext(MessageContext ctx) {
        String name = "[<b>Неизвестный</b>]";
        Long userId = ctx.getUserId();
        if (userId != null) {
            Player p = playerRepository.getOrCreatePlayer(userId);
            if (p != null && hasText(p.getName())) {
                name = "[<b>" + AnswerMethodFactory.escapeHtml(p.getName()) + "</b>]";
            }
        }
        return name;
    }

    // ================== ОТПРАВКА ==================

    public void wrapAndSend(Message message) {
        notificationExecutor.execute(() -> handleUserMessage(message));
    }

    public Optional<Integer> sendMessageSuperAdmin(String text) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(SUPERADMIN_ID, text, null);
        return execute(() -> telegramClient.execute(sendMessage)).map(Message::getMessageId);
    }

    public Optional<Integer> sendChatMessage(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage)).map(Message::getMessageId);
    }

    // ================== МОДЕРАЦИЯ ==================

    private void processSendId(Integer replyToMsgId) {
        MessageContext messageContext = messageRepository.findMessageContextByMessageId(replyToMsgId);
        Long userId = Optional.ofNullable(messageContext).map(MessageContext::getUserId).orElse(null);
        sendMessageSuperAdmin("ID пользователя");
        sendMessageSuperAdmin(userId + "");
    }

    private void processBan(String banDays, Integer replyToMessageId, Message message) {
        MessageContext messageContext = messageRepository.findMessageContextByMessageId(replyToMessageId);
        Long userId = Optional.ofNullable(messageContext).map(MessageContext::getUserId).orElse(null);

        if (userId == null || isUserAdmin(userId) || isUserSuperAdmin(userId)) {
            return;
        }

        Integer days = extractBanDays(banDays);
        Player player = playerRepository.getOrCreatePlayer(userId);
        if (days != null) {
            days = days < 30 ? days : 30;
            playerRepository.setBan(days, userId);
        } else {
            days = 1;
            playerRepository.setBan(days, userId);
        }
        sendMessageSuperAdmin(String.format("Пользователь userId: [%s]\nКоличество дней бана [%d]", userId, days));
        sendChatMessage(String.format("Пользователь: [ %s ]\nКоличество дней бана [ %d ]", player.getName(), days), message.getChatId());
        executeKickUser(userId, messageContext.getSenderChatId());
    }

    private boolean isBanText(String text) {
        if (!hasText(text)) return false;
        text = text.toLowerCase();
        return text.matches("бан( [1-9]\\d{0,9})?");
    }

    private boolean isUserText(String text) {
        if (!hasText(text)) return false;
        text = text.toLowerCase();
        return "user".equals(text);
    }

    private Integer extractBanDays(String text) {
        if (!hasText(text)) return null;
        text = text.toLowerCase();
        if (!text.startsWith("бан ")) return null;
        try {
            return Integer.parseInt(text.substring(4));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ================== DELETING ==================

    private DeleteMessage getDeleteMessage(Message message) {
        return DeleteMessage.builder()
                .chatId(String.valueOf(message.getChatId()))
                .messageId(message.getMessageId())
                .build();
    }

    private void executeDeleteMessage(Message message) {
        execute(() -> {
            try {
                telegramClient.execute(getDeleteMessage(message));
            } catch (TelegramApiException e) {
                log.error("Ошибка при удалении сообщения", e);
            }
            return null;
        });
    }

    private void executeKickUser(Long userId, Long chatId) {
        BanChatMember banChatMember = BanChatMember.builder()
                .chatId(String.valueOf(chatId))
                .userId(userId)
                .build();

        UnbanChatMember unbanChatMember = UnbanChatMember.builder()
                .chatId(String.valueOf(chatId))
                .userId(userId)
                .onlyIfBanned(true)
                .build();

        execute(() -> {
            try {
                telegramClient.execute(banChatMember);
                telegramClient.execute(unbanChatMember);
            } catch (TelegramApiException e) {
                log.error("Ошибка при кике пользователя {} из чата {}", userId, chatId, e);
            }
            return null;
        });
    }

    private void executeKickUser(Message message) {
        Long userId = message.getFrom().getId();
        String chatId = String.valueOf(message.getChatId());

        BanChatMember banChatMember = BanChatMember.builder()
                .chatId(chatId)
                .userId(userId)
                .build();

        UnbanChatMember unbanChatMember = UnbanChatMember.builder()
                .chatId(chatId)
                .userId(userId)
                .onlyIfBanned(true)
                .build();

        execute(() -> {
            try {
                telegramClient.execute(banChatMember);
                telegramClient.execute(unbanChatMember);
            } catch (TelegramApiException e) {
                log.error("Ошибка при кике пользователя {} из чата {}", userId, chatId, e);
            }
            return null;
        });
    }

    // ================== ХЕЛПЕРЫ ==================

    private String getText(Message message) {
        return message.getText() != null
                ? message.getText()
                : message.getCaption() != null
                ? message.getCaption()
                : "";
    }

    private boolean isBanned(Message message) {
        User from = message.getFrom();
        if (from == null) return true;
        Player player = playerRepository.getOrCreatePlayer(from.getId());
        return Optional.ofNullable(player.getBanEndAt())
                .map(end -> LocalDateTime.now().isBefore(end))
                .orElse(false);
    }

    private Integer getReplyToMessage(Message message) {
        if (message.isReply()) {
            return message.getReplyToMessage().getMessageId();
        }
        return null;
    }

    private String buildSenderName(Message message) {
        User from = message.getFrom();
        if (from == null) return "[<b>Неизвестный</b>]";
        Player byId = playerRepository.getOrCreatePlayer(from.getId());
        if (byId == null) return "[<b>Неизвестный</b>]";
        return "[<b>" + AnswerMethodFactory.escapeHtml(byId.getName()) + "</b>]";
    }

    // ================== EXECUTE ==================

    @FunctionalInterface
    private interface TelegramAction<T> {
        T run() throws TelegramApiException;
    }

    private <T> Optional<T> execute(TelegramAction<T> action) {
        try {
            return Optional.ofNullable(action.run());
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isUserAdmin(Message message) {
        Set<Long> adminIds = telegramProperties.getAdminIds();
        if (adminIds == null || adminIds.isEmpty()) return false;
        return adminIds.contains(message.getFrom().getId());
    }

    private boolean isUserAdmin(Long userId) {
        Set<Long> adminIds = telegramProperties.getAdminIds();
        if (adminIds == null || adminIds.isEmpty()) return false;
        return adminIds.contains(userId);
    }

    private boolean isUserSuperAdmin(Long userId) {
        return SUPERADMIN_ID.equals(userId);
    }

    private boolean isUserSuperAdmin(Message message) {
        return SUPERADMIN_ID.equals(message.getFrom().getId());
    }

    private Long extractUserId(Message message) {
        if (message == null) return null;
        if (message.getFrom() != null) return message.getFrom().getId();
        if (message.getSenderChat() != null) return message.getSenderChat().getId();
        if (message.getForwardFrom() != null) return message.getForwardFrom().getId();
        return null;
    }

    private Optional<?> executeGeneric(BotApiMethod<?> method) {
        try {
            return Optional.ofNullable(telegramClient.execute(method));
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ================== ОСТАЛЬНЫЕ ==================

    private boolean hasPhotoMsg(MediaMsgModel message) {
        return Optional.ofNullable(message).map(MediaMsgModel::getPhotoResponses).map(p -> !p.isEmpty()).orElse(false);
    }

    private boolean hasVideoMsg(MediaMsgModel message) {
        return Optional.ofNullable(message).map(MediaMsgModel::getVideoResponses).map(v -> !v.isEmpty()).orElse(false);
    }

    private boolean hasApiMsg(MediaMsgModel message) {
        return Optional.ofNullable(message).map(MediaMsgModel::getBotApiMethods).map(a -> !a.isEmpty()).orElse(false);
    }

    private void sleep() {
        try { Thread.sleep(1000); } catch (Exception ignored) {}
    }

    private boolean hasMedeaGroup(MediaMsgModel message) {
        return message.getMediaGroup() != null;
    }

    public void deleteMessage(DeleteMessage deleteMessage) {
        executeGeneric(deleteMessage);
    }

    public void deleteMessage(Update update) {
        DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(update);
        if (deleteMessage != null) executeGeneric(deleteMessage);
    }
}
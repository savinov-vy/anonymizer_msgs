package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
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
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.MediaMsgModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.MessageRepository;
import ru.network.anonymizer_msgs.repository.PlayerRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SenderService implements AdminNotifier {

    TelegramClient telegramClient;
    PlayerRepository playerRepository;
    Executor notificationExecutor;
    private final MessageRepository messageRepository;


    public void wrapAndSend(Message message) {
        notificationExecutor.execute(() -> sendUserMessage(message));
    }

    /**
     * Отправляет сообщение и возвращает messageId.
     */
    public Optional<Integer> sentMessage(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public Optional<Integer> sendHTMLMessage(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getHTMLSendMessage(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public void sendUserMessage(Message original) {
        if (isBanned(original)) {
            executeKickUser(original);
            return;
        }

        String senderName = buildSenderName(original);
        // Получаем текст (подпись для медиа или сам текст)
        String text = original.getText() != null ? original.getText() : original.getCaption() != null ? original.getCaption() : "";
        String newText = senderName + "\n" + text;

        // Получаем ID сообщения, на которое отвечаем (если это Reply)
        Integer replyToMsgId = getReplyToMessage(original);

        Message sentMessage = null;

        try {
            // --- ЛОГИКА ОПРЕДЕЛЕНИЯ ТИПА ---
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
                // Ваш старый метод для текста
                SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(
                        original.getChatId(), replyToMsgId, newText, null);
                sentMessage = telegramClient.execute(sendMessage);
            }

            // --- СОХРАНЕНИЕ В MAP ---
            if (sentMessage != null) {
                // Определяем fileId для сохранения (если это медиа)
                String fileIdToSave = null;
                if (original.hasPhoto())
                    fileIdToSave = original.getPhoto().get(original.getPhoto().size() - 1).getFileId();
                else if (original.hasVideo()) fileIdToSave = original.getVideo().getFileId();
                else if (original.hasVoice()) fileIdToSave = original.getVoice().getFileId();

                messageRepository.saveMessage(
                        sentMessage.getMessageId(),
                        original.getChatId(),
                        original.getMessageId(),
                        fileIdToSave,               // Сохраняем fileId
                        text                        // Сохраняем текст
                );
            }

        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
            return; // Прерываем, чтобы не удалять оригинал
        }

        // --- УДАЛЕНИЕ ОРИГИНАЛА ---
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

    private void executeKickUser(Message message) {
        Long userId = message.getFrom().getId();
        BanChatMember banChatMember = BanChatMember.builder()
                .chatId(String.valueOf(message.getChatId()))
                .userId(userId)
                .revokeMessages(true)
                .build();
        execute(() -> {
            try {
                telegramClient.execute(banChatMember);
            } catch (TelegramApiException e) {
                log.error("Ошибка при удалении сообщения", e);
            }
            return null;
        });
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
            Message repliedTo = message.getReplyToMessage();

            return repliedTo.getMessageId(); // 71
        }
        return null;
    }

    private String buildSenderName(Message message) {
        User from = message.getFrom();
        if (from == null) return "Неизвестный:";
        Player byId = playerRepository.getOrCreatePlayer(from.getId());
        if (byId == null) return "Неизвестный:";
        String name = byId.getName();
        return name + ":\n";
    }

    // ================== вспомогательное ==================

    private boolean hasPhotoMsg(MediaMsgModel message) {
        return Optional.ofNullable(message)
                .map(MediaMsgModel::getPhotoResponses)
                .map(photos -> !photos.isEmpty())
                .orElse(false);
    }

    private boolean hasVideoMsg(MediaMsgModel message) {
        return Optional.ofNullable(message)
                .map(MediaMsgModel::getVideoResponses)
                .map(video -> !video.isEmpty())
                .orElse(false);
    }

    private boolean hasApiMsg(MediaMsgModel message) {
        return Optional.ofNullable(message)
                .map(MediaMsgModel::getBotApiMethods)
                .map(apis -> !apis.isEmpty())
                .orElse(false);
    }

    private void sleep() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }


    private boolean hasMedeaGroup(MediaMsgModel message) {
        return message.getMediaGroup() != null;
    }


    public void deleteMessage(DeleteMessage deleteMessage) {
        executeGeneric(deleteMessage);
    }

    public void deleteMessage(Update update) {
        DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(update);
        if (deleteMessage == null) {
            return;
        }
        executeGeneric(deleteMessage);
    }
    // ================== execute ==================

    @FunctionalInterface
    private interface TelegramAction<T> {
        T run() throws TelegramApiException;
    }

    /**
     * Выполняет действие и возвращает результат, если он есть.
     * Ошибки логируются и превращаются в Optional.empty().
     */
    private <T> Optional<T> execute(TelegramAction<T> action) {
        try {
            return Optional.ofNullable(action.run());
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Обёртка для BotApiMethod с неизвестным типом (wildcard).
     */
    private Optional<?> executeGeneric(BotApiMethod<?> method) {
        try {
            return Optional.ofNullable(telegramClient.execute(method));
        } catch (TelegramApiException e) {
            System.err.println("Ошибка Telegram API: " + e.getMessage());
            return Optional.empty();
        }
    }
}
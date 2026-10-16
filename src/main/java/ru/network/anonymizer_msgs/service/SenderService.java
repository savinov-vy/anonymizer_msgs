package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.MediaMsgModel;
import ru.network.anonymizer_msgs.model.Player;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SenderService implements AdminNotifier {

    TelegramClient telegramClient;

    private static final Long CHAT_ID = -1004434014698L;
    private static final Integer TOPIC_MAFIA_ID = 5;

    // ================== системные/простые отправки ==================

    /**
     * Отправляет системное сообщение и возвращает messageId.
     */
    public Optional<Integer> sendSystemMessage(String chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    /**
     * Отправляет сообщение и возвращает messageId.
     */
    public Optional<Integer> sentMessage(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    /**
     * Отправляет сообщение в канал/чат по имени и возвращает messageId.
     */
    public Optional<Integer> sentMessage(String text, String channelName) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(channelName, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    /**
     * Отправляет сообщение с клавиатурой и возвращает messageId.
     */
    public Optional<Integer> sentMessage(String text, Long chatId, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(chatId, text, keyboardMarkup);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public Optional<Integer> sendHTMLMessage(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getHTMLSendMessage(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public Optional<Integer> sendRedirectableMsg(String text, Long chatId) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(chatId, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    // ================== MAFIA ==================

    public Optional<Integer> sendMafiaMessage(String text) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(CHAT_ID, TOPIC_MAFIA_ID, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public Optional<Integer> editMafiaMessage(String text, Integer messageId) {
        EditMessageText editMessage = AnswerMethodFactory.getEditMessage(CHAT_ID, messageId, text, null);
        return executeGeneric(editMessage).map(m -> m instanceof Message msg ? msg.getMessageId() : null);
    }

    public Optional<Integer> sendMafiaMessage(String text, Integer messageId) {
        CompletableFuture.runAsync(() -> {
            try {
                CopyMessage copyMessage = CopyMessage.builder()
                        .fromChatId(CHAT_ID)
                        .messageId(messageId)
                        .chatId(CHAT_ID)
                        .messageThreadId(TOPIC_MAFIA_ID)
                        .disableNotification(true)
                        .build();
                execute(() -> telegramClient.execute(copyMessage));

                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(CHAT_ID)
                        .messageId(messageId)
                        .build();
                execute(() -> telegramClient.execute(deleteMessage));
            } catch (Exception e) {
                System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
            }
        });

        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(CHAT_ID, TOPIC_MAFIA_ID, text, null);
        return execute(() -> telegramClient.execute(sendMessage))
                .map(Message::getMessageId);
    }

    public Optional<Integer> sendMafiaMessage(Integer messageId) {
        CompletableFuture.runAsync(() -> {
            try {
                CopyMessage copyMessage = CopyMessage.builder()
                        .fromChatId(CHAT_ID)
                        .messageId(messageId)
                        .chatId(CHAT_ID)
                        .disableNotification(true)
                        .messageThreadId(TOPIC_MAFIA_ID)
                        .build();
                execute(() -> telegramClient.execute(copyMessage));

                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(CHAT_ID)
                        .messageId(messageId)
                        .build();
                execute(() -> telegramClient.execute(deleteMessage));
            } catch (Exception e) {
                System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
            }
        });
        return Optional.empty();
    }

    // ================== низкоуровневые ==================

    /**
     * Отправляет любой BotApiMethod и возвращает результат (Message, если метод его возвращает).
     */
    public <T extends Serializable> Optional<T> sendNowPrivate(BotApiMethod<T> toSend) {
        return execute(() -> telegramClient.execute(toSend));
    }

    /**
     * Отправляет SendMessage и возвращает Message (можно получить messageId).
     */
    public Optional<Message> sendNowChat(SendMessage message) {
        return execute(() -> telegramClient.execute(message));
    }

    /**
     * Отправляет текст пользователю и возвращает messageId.
     */
    public Optional<Integer> sendNowPrivate(String text, Long toUserId) {
        SendMessage message = AnswerMethodFactory.getSendMessage(toUserId, text, null);
        return execute(() -> telegramClient.execute(message))
                .map(Message::getMessageId);
    }

    /**
     * Отправляет текст пользователю и возвращает messageId.
     */
    public void sendNowPrivate(String text, List<Player> toUsers) {
        for (Player p : toUsers) {
            SendMessage message = AnswerMethodFactory.getSendMessage(p.getId(), text, null);
            executeGeneric(message);
            sleep();
        }
    }

    public Optional<Message> sendNowPrivate(SendVideo message) {
        return execute(() -> telegramClient.execute(message));
    }

    // ================== Media ==================

    /**
     * Отправляет медиа-модель. Возвращает messageId последнего успешно отправленного сообщения
     * (или пусто, если ничего не отправлялось).
     */
    public Optional<Integer> sendNowPrivate(MediaMsgModel message) {
        if (!(hasPhotoMsg(message)
                || hasApiMsg(message)
                || hasMedeaGroup(message)
                || hasVideoMsg(message))) {
            return Optional.empty();
        }

        Optional<Integer> lastMessageId = Optional.empty();

        if (hasPhotoMsg(message)) {
            for (SendPhoto photo : message.getPhotoResponses()) {
                lastMessageId = execute(() -> telegramClient.execute(photo))
                        .map(Message::getMessageId);
            }
        }

        if (hasVideoMsg(message)) {
            for (SendVideo video : message.getVideoResponses()) {
                lastMessageId = execute(() -> telegramClient.execute(video))
                        .map(Message::getMessageId);
            }
        }

        if (hasApiMsg(message)) {
            for (BotApiMethod<?> answer : message.getBotApiMethods()) {
                lastMessageId = executeGeneric(answer)
                        .map(m -> m instanceof Message msg ? msg.getMessageId() : null);
            }
        }

        return lastMessageId;
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
        } catch (Exception e) {}
    }


    private boolean hasMedeaGroup(MediaMsgModel message) {
        return message.getMediaGroup() != null;
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
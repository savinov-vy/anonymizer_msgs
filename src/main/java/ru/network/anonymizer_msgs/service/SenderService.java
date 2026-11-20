package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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

// Инжектим ваш репозиторий

    public void sendUserMessage(Message original) {
        String senderName = buildSenderName(original);
        String originalText = original.getText() != null ? original.getText() : "";
        String newText = senderName + "\n" + originalText;

        // 1. Формируем сообщение (отвечаем на original.getMessageId())
        SendMessage sendMessage = AnswerMethodFactory.getSendMessageRedirectable(
                original.getChatId(),
                getReplyToMessage(original), // Теперь replyParameters будет работать
                newText,
                null
        );

        // 2. Отправляем СИНХРОННО, чтобы получить ID нового сообщения
        try {
            Message sentMessage = telegramClient.execute(sendMessage);

            // 3. Сохраняем в Map: ID нового сообщения в чате -> контекст пользователя
            if (sentMessage != null) {
                messageRepository.saveMessage(
                        sentMessage.getMessageId(), // ID сообщения в анонимном чате
                        original.getChatId(),       // Кому отвечать (ID в личке)
                        original.getMessageId(),    // ID в личке
                        null,                       // fileId (пока null, т.к. только текст)
                        originalText
                );
            }
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения", e);
            // Если не удалось отправить — прерываем выполнение, чтобы не удалять оригинал
            return;
        }

        // 4. Удаляем оригинал (делаем это ПОСЛЕ успешной отправки)
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(String.valueOf(original.getChatId())) // telegramClient ожидает String
                .messageId(original.getMessageId())
                .build();

        execute(() -> {
            try {
                telegramClient.execute(deleteMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при удалении сообщения", e);
            }
            return null;
        });
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
package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.util.MediaGroupBuffer;

import java.util.concurrent.CompletableFuture;

@Component
public class WebHookBot {

    private final TelegramClient telegramClient;
    private final MediaGroupBuffer mediaGroupBuffer;

    public WebHookBot(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.mediaGroupBuffer = new MediaGroupBuffer(telegramClient); // Инициализируем буфер альбомов
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.getFrom() != null && message.getFrom().getIsBot()) {
                return null;
            }

            // РЕШЕНИЕ: Если у сообщения есть MediaGroupId — отправляем его в накопительный буфер
            if (message.getMediaGroupId() != null) {
                mediaGroupBuffer.addMessage(message);
                return null;
            }

            // --- Логика для ОДИНОЧНЫХ сообщений (текст, голос, одно фото) ---
            String chatId = message.getChatId().toString();
            Integer messageId = message.getMessageId();
            Integer threadId = message.getMessageThreadId();

            CompletableFuture.runAsync(() -> {
                try {
                    CopyMessage copyMessage = CopyMessage.builder()
                            .fromChatId(chatId)
                            .messageId(messageId)
                            .chatId(chatId)
                            .messageThreadId(threadId)
                            .build();
                    telegramClient.execute(copyMessage);

                    DeleteMessage deleteMessage = DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build();
                    telegramClient.execute(deleteMessage);
                } catch (Exception e) {
                    System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
                }
            });
        }
        return null;
    }
}

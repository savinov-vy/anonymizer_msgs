package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.CompletableFuture;

@Component
public class WebHookBot {

    private final TelegramClient telegramClient;

    public WebHookBot(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            System.out.println("!!! СРАБОТАЛ ПЕРЕХВАТЧИК !!! Получено сообщение: " + message.getText());

            String chatId = message.getChatId().toString();
            Integer messageId = message.getMessageId();

            CompletableFuture.runAsync(() -> {
                try {
                    DeleteMessage deleteMessage = DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build();

                    telegramClient.execute(deleteMessage);
                    System.out.println(">>> Успешно удалено сообщение ID: " + messageId);
                } catch (TelegramApiException e) {
                    System.err.println(">>> Ошибка удаления через TelegramClient: " + e.getMessage());
                }
            });
        }
        return null;
    }
}

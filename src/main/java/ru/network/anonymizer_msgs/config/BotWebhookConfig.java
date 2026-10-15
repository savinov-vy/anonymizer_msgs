package ru.network.anonymizer_msgs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import ru.network.anonymizer_msgs.controller.WebHookBot;

@Configuration
public class BotWebhookConfig {

    @Bean
    public SpringTelegramWebhookBot springTelegramWebhookBot(
            WebHookBot webHookBot,
            TelegramClient telegramClient,
            TelegramProperties properties) {

        // Достаем значения из распарсенных свойств
        String botPath = properties.getBotPath(); // Переменная "8947815756"
        String botBaseUrl = properties.getBotName(); // Переменная "https://dbcc-151-245-216-50.ngrok-free.app"

        String fullWebhookUrl = botBaseUrl + "/callback/" + botPath;

        // Собираем бин, который стартер библиотеки внедрит во внутренний контроллер
        return SpringTelegramWebhookBot.builder()
                .botPath(botPath) // Библиотека откроет путь: /callback/8947815756
                .updateHandler(webHookBot::handleUpdate) // Привязываем ваш метод обработки сообщений

                // Лямбда регистрации вебхука
                .setWebhook(() -> {
                    try {
                        SetWebhook setWebhookRequest = SetWebhook.builder()
                                .url(fullWebhookUrl)
                                .build();
                        telegramClient.execute(setWebhookRequest);
                        System.out.println(">>> Webhook успешно зарегистрирован в Telegram: " + fullWebhookUrl);
                    } catch (TelegramApiException e) {
                        System.err.println(">>> Ошибка при выполнении SetWebhook: " + e.getMessage());
                    }
                })

                // Лямбда удаления вебхука
                .deleteWebhook(() -> {
                    try {
                        telegramClient.execute(new DeleteWebhook());
                        System.out.println(">>> Webhook успешно удален с серверов Telegram.");
                    } catch (TelegramApiException e) {
                        System.err.println(">>> Ошибка при выполнении DeleteWebhook: " + e.getMessage());
                    }
                })
                .build();
    }
}

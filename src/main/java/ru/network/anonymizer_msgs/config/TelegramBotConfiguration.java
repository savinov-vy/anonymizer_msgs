package ru.network.anonymizer_msgs.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@AllArgsConstructor
public class TelegramBotConfiguration {

    // Считываем свойства из префикса telegrambots (класс TelegramProperties)
    private final TelegramProperties telegramProperties;

    /**
     * Вручную создаем бин клиента, чтобы Spring Boot смог внедрить его в ваш WebHookBot
     */
    @Bean
    public TelegramClient telegramClient() {
        // Передаем токен вашего бота
        return new OkHttpTelegramClient(telegramProperties.getBotToken());
    }

}

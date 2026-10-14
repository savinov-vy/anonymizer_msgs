package ru.network.anonymizer_msgs.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
@AllArgsConstructor
public class TelegramBotConfiguration {

	// Считываем токен из application.yml (ключ telegrambots.bot-token)
	TelegramProperties telegramProperties;

	/**
	 * Определяем бин TelegramClient вручную.
	 * Spring будет знать об этом бине и сможет внедрить его в другие классы.
	 */
	@Bean
	public TelegramClient telegramClient() {
		return new OkHttpTelegramClient(telegramProperties.getBotToken());
	}
}

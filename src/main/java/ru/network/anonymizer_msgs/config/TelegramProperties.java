package ru.network.anonymizer_msgs.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "telegrambots")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TelegramProperties {
	/**
	 * уникальный токен бота
	 */
	String botToken;

	String botUsername;

	String botPath;

	/**
	 * пароль админа
	 */
	String adminPassword;

	/**
	 * Включен, отключен бот
	 */
	Boolean isBotEnabled;

	/**
	 * Промо анкета
	 * с рекламой и взаимопиаром
	 */
	Long promoReelsId;

	/**
	 * канал для взаимодействия с ботом
	 */
	String channelName;

	/**
	 * наименование бота
	 */
	String botName;
}

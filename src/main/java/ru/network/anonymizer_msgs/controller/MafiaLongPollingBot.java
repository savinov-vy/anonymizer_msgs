package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.config.TelegramProperties;

import java.util.List;

@Component // Благодаря этому стартер автоматически подхватит и зарегистрирует бота
public class MafiaLongPollingBot implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final WebHookBot webHookBot;
    private final TelegramProperties telegramProperties;

    // Внедряем ваш существующий WebHookBot и конфигурационные свойства TelegramProperties
    public MafiaLongPollingBot(WebHookBot webHookBot, TelegramProperties telegramProperties) {
        this.webHookBot = webHookBot;
        this.telegramProperties = telegramProperties;
    }

    @Override
    public String getBotToken() {
        return this.telegramProperties.getBotToken(); // Берем токен из конфигурационного файла
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this; // Указываем, что этот же класс будет обрабатывать обновления
    }

    @Override
    public void consume(List<Update> updates) {
        // Передаем входящие сообщения в ваш готовый handleUpdate контроллера WebHookBot
        for (Update update : updates) {
            try {
                webHookBot.handleUpdate(update);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке апдейта в MafiaLongPollingBot: " + e.getMessage());
            }
        }
    }

    /**
     * Системное уведомление: выполнится автоматически сразу после успешного подключения к серверам Telegram
     */
    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("🤖 Бот успешно запущен локально в режиме Long Polling без использования вебхуков и доменов!");
    }
}

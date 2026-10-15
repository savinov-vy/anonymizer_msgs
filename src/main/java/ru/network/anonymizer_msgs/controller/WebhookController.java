package ru.network.anonymizer_msgs.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final WebHookBot webHookBot;

    // Внедряем ваш готовый обработчик
    public WebhookController(WebHookBot webHookBot) {
        this.webHookBot = webHookBot;
    }

    /**
     * Явный эндпоинт для приема POST-запросов от Telegram через ngrok.
     * Замените "8947815756" на значение вашего telegrambots.bot-path из yml,
     * либо оставьте строкой, если он фиксированный.
     */
    @PostMapping("/callback/8947815756")
    public BotApiMethod<?> receiveUpdate(@RequestBody Update update) {
        // Перенаправляем чистый JSON-объект в ваш метод удаления сообщений
        return webHookBot.handleUpdate(update);
    }
}


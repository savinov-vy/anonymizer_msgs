package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.network.anonymizer_msgs.config.TelegramProperties;
import ru.network.anonymizer_msgs.service.SenderService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AnonymizerLongPooling implements SpringLongPollingBot, LongPollingUpdateConsumer {

    private final TelegramProperties telegramProperties;
    private final SenderService senderService;

    private final Map<Long, Long> lastSeen = new ConcurrentHashMap<>();
    private final AtomicLong windowStartMs = new AtomicLong();
    private final AtomicInteger globalCounter = new AtomicInteger();

    public AnonymizerLongPooling(TelegramProperties telegramProperties, SenderService senderService) {
        this.telegramProperties = telegramProperties;
        this.senderService = senderService;
    }

    @Override
    public String getBotToken() {
        return this.telegramProperties.getBotToken(); // Берем токен из конфигурационного файла
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(List<Update> updates) {

        /**
         * защита от спама, но загрузка медиа разрешена т.к. она делается
         * поочереди и быстро
         */

        // Передаем входящие сообщения в ваш готовый handleUpdate контроллера WebHookBot
        for (Update update : updates) {
            Long userId = extractUserId(update);
            if (!allowGlobal() || !(allow(userId) || hasMedia(update))) {
                return;
            }
            try {
                if (update.hasMessage()) {
                    Message message = update.getMessage();
                    senderService.handleUserMessage(message);
                }
            } catch (Exception e) {
                System.err.println("Ошибка при обработке апдейта в MafiaLongPollingBot: " + e.getMessage());
            }
        }
    }

    private Long extractUserId(Update update) {
        org.telegram.telegrambots.meta.api.objects.User from = null;

        if (update.hasMessage()) {
            from = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            from = update.getCallbackQuery().getFrom();
        } else if (update.getMyChatMember() != null) {
            from = update.getMyChatMember().getFrom();
        }

        if (from != null) {
            return from.getId();
        }

        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        return null;
    }

    //TODO утечка памяти 100тыс -> 10 Мб
    boolean allow(Long userId) {
        long now = System.currentTimeMillis();
        Long prev = lastSeen.put(userId, now);
        return prev == null || (now - prev) >= 500; // 1 сек
    }

    boolean allowGlobal() {
        long now = System.currentTimeMillis();
        long start = windowStartMs.get();
        if (now - start > 1000 && windowStartMs.compareAndSet(start, now)) {
            globalCounter.set(0);
        }
        return globalCounter.incrementAndGet() <= 20;
    }

    private boolean hasMedia(Update update) {
        return update.hasMessage() && (update.getMessage().hasVideo() || update.getMessage().hasPhoto());
    }

    /**
     * Системное уведомление: выполнится автоматически сразу после успешного подключения к серверам Telegram
     */
    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        System.out.println("🤖 Бот успешно запущен локально в режиме Long Polling без использования вебхуков и доменов!");
    }
}

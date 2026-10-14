package ru.network.anonymizer_msgs.controller;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.config.TelegramProperties;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainTgDispatcher implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    TelegramProperties telegramProperties;
    private final Map<Long, Long> lastSeen = new ConcurrentHashMap<>();
    private final AtomicLong windowStartMs = new AtomicLong();
    private final AtomicInteger globalCounter = new AtomicInteger();


    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public String getBotToken() {
        return telegramProperties.getBotToken();
    }

    @Override
    public void consume(Update update) {
        Long userId = extractUserId(update);

        if (userId == null) {
            return;
        }
        /**
         * защита от спама, но загрузка медиа разрешена т.к. она делается
         * поочереди и быстро
         */
        if (!allowGlobal() || !(allow(userId) || hasMedia(update))) {
            return;
        }


        if (update.hasMessage() && !update.getMessage().isUserMessage()) {
            return;
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
        return prev == null || (now - prev) >= 1000; // 1 сек
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

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}

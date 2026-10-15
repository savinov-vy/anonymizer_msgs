package ru.network.anonymizer_msgs.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.*;

public class MediaGroupBuffer {

    private final TelegramClient telegramClient;
    // Буфер: MediaGroupId -> Список сообщений, входящих в этот альбом
    private final Map<String, List<Message>> buffer = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MediaGroupBuffer(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void addMessage(Message message) {
        String mediaGroupId = message.getMediaGroupId();

        // Если это не альбом, обрабатываем мгновенно (код старого зеркала)
        if (mediaGroupId == null) {
            return;
        }

        // Кладем сообщение в список текущего альбома
        buffer.computeIfAbsent(mediaGroupId, k -> {
            List<Message> list = Collections.synchronizedList(new ArrayList<>());
            // Запускаем таймер на 300 миллисекунд. Ждем, пока прилетят остальные фото альбома
            scheduler.schedule(() -> sendAlbum(mediaGroupId), 300, TimeUnit.MILLISECONDS);
            return list;
        }).add(message);
    }

    private void sendAlbum(String mediaGroupId) {
        List<Message> messages = buffer.remove(mediaGroupId);
        if (messages == null || messages.isEmpty()) return;

        // Сортируем сообщения по ID, чтобы фотографии в альбоме шли в правильном порядке
        messages.sort(Comparator.comparing(Message::getMessageId));

        Message firstMsg = messages.get(0);
        String chatId = firstMsg.getChatId().toString();
        Integer threadId = firstMsg.getMessageThreadId();

        List<InputMedia> mediaList = new ArrayList<>();

        // Собираем коллекцию объектов для SendMediaGroup
        for (Message msg : messages) {
            if (msg.hasPhoto()) {
                // Берем самое лучшее качество фотографии (последний элемент в списке)
                String fileId = msg.getPhoto().get(msg.getPhoto().size() - 1).getFileId();
                InputMediaPhoto photo = InputMediaPhoto.builder()
                        .media(fileId)
                        .caption(msg.getCaption()) // Сохраняем исходный текст подписи
                        .build();
                mediaList.add(photo);
            }
        }

        try {
            if (!mediaList.isEmpty()) {
                // 1. Отправляем фотографии одной пачкой (альбомом) от имени бота
                SendMediaGroup sendMediaGroup = SendMediaGroup.builder()
                        .chatId(chatId)
                        .messageThreadId(threadId)
                        .medias(mediaList)
                        .build();
                telegramClient.execute(sendMediaGroup);
            }

            // 2. Только после успешной отправки альбома удаляем все оригиналы пользователя
            for (Message msg : messages) {
                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(chatId)
                        .messageId(msg.getMessageId())
                        .build();
                telegramClient.execute(deleteMessage);
            }
            System.out.println(">>> Альбом зазеркален и оригиналы удалены!");

        } catch (Exception e) {
            System.err.println(">>> Ошибка при зеркалировании альбома: " + e.getMessage());
        }
    }
}

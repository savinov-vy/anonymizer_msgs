package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageRepository {

    // Структура для хранения контекста
    public static class MessageContext {
        public final Long senderChatId;     // Кто написал (ID в личке)
        public final Integer originalMsgId; // ID оригинального сообщения (если нужно)
        public final String fileId;         // Если это медиа
        public final String text;           // Если это текст

        public MessageContext(Long senderChatId, Integer originalMsgId, String fileId, String text) {
            this.senderChatId = senderChatId;
            this.originalMsgId = originalMsgId;
            this.fileId = fileId;
            this.text = text;
        }
    }

    // Ключ: ID сообщения в анонимном чате. Значение: контекст
    private static final ConcurrentHashMap<Integer, MessageContext> messagesMap = new ConcurrentHashMap<>();

    public static void saveMessage(Integer publicMsgId, Long senderChatId, Integer originalMsgId, String fileId, String text) {
        messagesMap.put(publicMsgId, new MessageContext(senderChatId, originalMsgId, fileId, text));
    }

    public static MessageContext getMessage(Integer publicMsgId) {
        return messagesMap.get(publicMsgId);
    }


    public static void cleanOldMessages() {
        // Можно удалять сообщения старше N часов
    }

}

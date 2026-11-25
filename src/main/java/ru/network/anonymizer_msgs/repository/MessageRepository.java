package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.model.MessageContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageRepository {

    private static final ConcurrentHashMap<Integer, MessageContext> messagesMap = new ConcurrentHashMap<>();

    public Long findUserIdByReplyToMessageId(Integer replyToMessageId) {
        for (Map.Entry<Integer, MessageContext> messageEntry : messagesMap.entrySet()) {
            MessageContext value = messageEntry.getValue();
            if (value == null || value.getReplyToMessageId() == null) {
                continue;
            }
            if (replyToMessageId.equals(value.getReplyToMessageId())) {
                return value.getUserId();
            }
        }
        return -1L;
    }

    public MessageContext findMessageContextByMessageId(Integer replyToMessageId) {
        if (replyToMessageId == null) {
            return null;

        }
        return messagesMap.get(replyToMessageId);
    }


    public static void saveMessage(Integer publicMsgId, Long senderChatId, Integer originalMsgId, String fileId, String text, Long userId, Integer replyToMessageId) {
        messagesMap.put(publicMsgId, new MessageContext(senderChatId, originalMsgId, fileId, text, userId, replyToMessageId));
    }

    public static MessageContext getMessage(Integer publicMsgId) {
        return messagesMap.get(publicMsgId);
    }


    public static void cleanOldMessages() {
        // Можно удалять сообщения старше N часов
    }

}

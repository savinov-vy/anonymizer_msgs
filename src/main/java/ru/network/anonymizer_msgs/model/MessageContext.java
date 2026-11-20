package ru.network.anonymizer_msgs.model;

import lombok.Data;
import ru.network.anonymizer_msgs.repository.MessageRepository;

import java.util.concurrent.ConcurrentHashMap;

@Data
public class MessageContext {
    public final Long senderChatId;
    public final Integer originalMsgId;
    public final String fileId;
    public final String text;
    public final Long userId;
    public final Integer replyToMessageId;

    public MessageContext(Long senderChatId,
                          Integer originalMsgId,
                          String fileId,
                          String text,
                          Long userId,
                          Integer replyToMessageId) {
        this.senderChatId = senderChatId;
        this.originalMsgId = originalMsgId;
        this.fileId = fileId;
        this.text = text;
        this.userId = userId;
        this.replyToMessageId = replyToMessageId;
    }
}
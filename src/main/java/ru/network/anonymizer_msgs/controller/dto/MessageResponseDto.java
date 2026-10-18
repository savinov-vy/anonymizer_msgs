package ru.network.anonymizer_msgs.controller.dto;

import lombok.Value;

@Value
public class MessageResponseDto {
    Long messageId;
    Long chatId;
    Long userId;       // кому отправлено (для /to-user)
    String text;
    String status;     // например: "SENT", "FAILED"
    Long timestamp;
}
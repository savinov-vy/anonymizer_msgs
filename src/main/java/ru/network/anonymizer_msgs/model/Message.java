package ru.network.anonymizer_msgs.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Message {
    private final Long id;
    private final String text;
    /**
     * жив или нет
     */
    private LocalDateTime banEndAt;
    private LocalDateTime registeredAt;
}

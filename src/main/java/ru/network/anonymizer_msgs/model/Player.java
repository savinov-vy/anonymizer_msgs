package ru.network.anonymizer_msgs.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Player {
    private final Long id;
    private final String name;
    /**
     * жив или нет
     */
    private LocalDateTime banEndAt;
    private LocalDateTime registeredAt;
}

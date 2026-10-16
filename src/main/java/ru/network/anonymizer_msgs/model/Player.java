package ru.network.anonymizer_msgs.model;

import lombok.Builder;
import lombok.Data;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.Role;

import java.time.LocalDateTime;

@Data
@Builder
public class Player {
    private final Long id;
    private final String name;
    private Role role;
    private ActionType actionType;
    private boolean isAlive;
    private LocalDateTime registeredAt;
}

package ru.network.anonymizer_msgs.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddPlayerModel {
    private final Player added;
    private final Integer amount;
}

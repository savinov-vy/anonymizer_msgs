package ru.network.anonymizer_msgs.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class SendMessageToUserDto {
    @NotBlank
    String text;
    @NotNull
    Long userId;
}

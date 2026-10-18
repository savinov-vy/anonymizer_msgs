package ru.network.anonymizer_msgs.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;


import java.util.List;

@Value
public class SendMessageToUsersDto {
    @NotBlank
    String text;
    @NotEmpty
    List<Long> userIds;
}
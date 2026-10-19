package ru.network.anonymizer_msgs.controller.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Data
@Value
@Builder
public class SendMessageFirstNightDto {
    @NotEmpty
    List<Long> userIds;
}
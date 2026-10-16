package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.CallBackDataPlayerIdDto;
import ru.network.anonymizer_msgs.utils.ObjectMapperUtils;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CallBackQueryService {

    public CallBackDataPlayerIdDto getCallBackData(Update update) {
        if (!update.hasCallbackQuery()) {
            return null;
        }
        String json = update.getCallbackQuery().getData();
        return ObjectMapperUtils.readValue(json, CallBackDataPlayerIdDto.class);
    }

}
package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.network.anonymizer_msgs.config.MafiaClientProperties;
import ru.network.anonymizer_msgs.controller.dto.MessageResponseDto;
import ru.network.anonymizer_msgs.controller.dto.SendMessageToUserDto;
import ru.network.anonymizer_msgs.controller.dto.SendMessageToUsersDto;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MafiaClientService {

    RestClient restClient;
    MafiaClientProperties mafiaClientProperties;
    ExecutorService mafiaSendExecutor;

    public MafiaClientService(RestClient restClient,
                              MafiaClientProperties mafiaClientProperties,
                              @Qualifier("mafiaSendExecutor") ExecutorService mafiaSendExecutor) {
        this.restClient = restClient;
        this.mafiaClientProperties = mafiaClientProperties;
        this.mafiaSendExecutor = mafiaSendExecutor;
    }

    public void sendToUser(String text, Long userId) {
        SendMessageToUserDto payload = new SendMessageToUserDto(text, userId);
        mafiaSendExecutor.submit(() -> executeWithRetry("/api/mafia/to-user", payload));
    }

    public void sendToUsers(String text, List<Long> userIds) {
        SendMessageToUsersDto payload = new SendMessageToUsersDto(text, userIds);
        mafiaSendExecutor.submit(() -> executeWithRetry("/api/mafia/to-users", payload));
    }

    private void executeWithRetry(String path, Object payload) {
        try {
            doPost(path, payload);
            log.debug("Mafia message delivered. path={}", path);
        } catch (Exception firstEx) {
            log.warn("Mafia send failed, retrying in {} ms. path={}, error={}",
                    mafiaClientProperties.getRetryDelayMs(), path, firstEx.getMessage());
            sleep(mafiaClientProperties.getRetryDelayMs());
            try {
                doPost(path, payload);
                log.debug("Mafia message delivered after retry. path={}", path);
            } catch (Exception secondEx) {
                log.error("Mafia send failed after retry. path={}", path, secondEx);
                // не пробрасываем — ответ никому не нужен
            }
        }
    }

    private MessageResponseDto doPost(String path, Object payload) {
        return restClient.post()
                .uri(mafiaClientProperties.getBaseUrl() + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(MessageResponseDto.class);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
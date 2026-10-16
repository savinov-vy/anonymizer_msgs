package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.model.Player;

import java.util.*;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MafiaGameService {

    private GamePhase currentPhase = GamePhase.FIRST_NIGHT;
    private final List<String> mafiaChatLog = new ArrayList<>();
    private final ThreadPoolTaskScheduler taskScheduler;
    private SenderService senderService;
    private PlayerRepository playerRepository;

    // Слушатель для уведомления бота о событиях (смена фазы, старт игры)
    private final Consumer<String> gameNotificationListener;


    private void notifyAllAsync(String message) {
        if (gameNotificationListener != null) {
            gameNotificationListener.accept("ALL:" + message);
        }
    }

    private void notifyUserDirectly(String userId, String message) {
        if (gameNotificationListener != null) {
            gameNotificationListener.accept(userId + ":" + message);
        }
    }

}

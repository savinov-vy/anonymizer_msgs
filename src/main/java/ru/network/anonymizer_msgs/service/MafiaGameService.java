package ru.network.anonymizer_msgs.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.model.Player;

import java.util.*;
import java.util.function.Consumer;

@Service
public class MafiaGameService {

    private GamePhase currentPhase = GamePhase.FIRST_NIGHT;
    private final List<String> mafiaChatLog = new ArrayList<>();
    private final ThreadPoolTaskScheduler taskScheduler;
    private SenderService senderService;
    private PlayerRepository playerRepository;

    // Слушатель для уведомления бота о событиях (смена фазы, старт игры)
    private Consumer<String> gameNotificationListener;

    public MafiaGameService(ThreadPoolTaskScheduler taskScheduler,
                            SenderService senderService,
                            PlayerRepository playerRepository) {
        this.taskScheduler = taskScheduler;
        this.senderService = senderService;
        this.playerRepository = playerRepository;
    }

    public void setRandomRoles() {
        // 1. Формируем пул из 10 спортивных ролей
        List<Role> rolePool = new ArrayList<>(List.of(
                Role.DON, Role.MAFIA, Role.MAFIA, // 3 черных
                Role.SHERIFF,                     // 1 шериф
                Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN // 6 мирных
        ));
        Collections.shuffle(rolePool);

        // 2. Раздаем роли игрокам в карте
        int i = 0;
        List<Player> players = playerRepository.getAll();
        for (Player player : players) {
            player.setRole(rolePool.get(i++));
            // Персонально уведомляем игрока о его роли
            senderService.sendNowPrivate( "🎮 Игра началась! Ваша секретная роль: " + player.getRole().name(), player.getId());
        }
    }

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

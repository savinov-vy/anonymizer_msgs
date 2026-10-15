package ru.network.anonymizer_msgs.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.enums.GamePhase;
import ru.network.anonymizer_msgs.enums.Role;
import ru.network.anonymizer_msgs.model.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Service
public class MafiaGameService {

    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private GamePhase currentPhase = GamePhase.FIRST_NIGHT;
    private final List<String> mafiaChatLog = new ArrayList<>();
    private final ThreadPoolTaskScheduler taskScheduler;

    // Слушатель для уведомления бота о событиях (смена фазы, старт игры)
    private Consumer<String> gameNotificationListener;

    public MafiaGameService(ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void setNotificationListener(Consumer<String> listener) {
        this.gameNotificationListener = listener;
    }

    /**
     * Динамическая регистрация игрока по команде /start
     * Возвращает true, если игра началась
     */
    public synchronized boolean registerPlayer(String id, String name) {
        if (players.containsKey(id)) {
            throw new IllegalStateException("Вы уже зарегистрированы в игре!");
        }
        if (players.size() >= 10) {
            throw new IllegalStateException("Извините, лобби уже заполнено (10/10).");
        }

        // Временно создаем игрока без роли (роль назначим при старте)
        players.put(id, new Player(id, name, null));

        notifyAllAsync("Игрок " + name + " присоединился! Всего игроков: " + players.size() + "/10");

        if (players.size() == 10) {
            startFirstNight();
            return true;
        }
        return false;
    }

    /**
     * Автоматический запуск игры при наборе 10 человек
     */
    private void startFirstNight() {
        this.currentPhase = GamePhase.FIRST_NIGHT;
        mafiaChatLog.clear();

        // 1. Формируем пул из 10 спортивных ролей
        List<Role> rolePool = new ArrayList<>(List.of(
                Role.DON, Role.MAFIA, Role.MAFIA, // 3 черных
                Role.SHERIFF,                     // 1 шериф
                Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN // 6 мирных
        ));
        Collections.shuffle(rolePool);

        // 2. Раздаем роли игрокам в карте
        int i = 0;
        for (Player player : players.values()) {
            player.setRole(rolePool.get(i++));
            // Персонально уведомляем игрока о его роли
            notifyUserDirectly(player.getId(), "🎮 Игра началась! Ваша секретная роль: " + player.getRole().name());
        }

        notifyAllAsync("🌌 Наступила ПЕРВАЯ НОЧЬ. У Мафии есть 60 секунд на договорку в чате бота!");

        // 3. Таймер на 60 секунд до наступления дня
        taskScheduler.schedule(
                () -> changePhase(GamePhase.DAY),
                Instant.now().plusSeconds(60)
        );
    }

    public void changePhase(GamePhase newPhase) {
        this.currentPhase = newPhase;

        if (newPhase == GamePhase.DAY) {
            notifyAllAsync("☀️ Наступил ДЕНЬ! Город просыпается. Мафия переходит в режим скрытого обсуждения.");
        } else if (newPhase == GamePhase.NIGHT) {
            notifyAllAsync("🌙 Наступила НОЧЬ! Чат мафии строго заблокирован. Время ночных действий.");

            // Таймер ночи на 45 секунд
            taskScheduler.schedule(
                    () -> changePhase(GamePhase.DAY),
                    Instant.now().plusSeconds(45)
            );
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

    // Все остальные методы (sendMafiaChatMessage, getPlayer, getAllPlayers) остаются без изменений
    public void sendMafiaChatMessage(String playerId, String message) {
        Player player = players.get(playerId);
        if (player == null || !player.isAlive()) throw new IllegalStateException("Игрок не найден или выбыл.");
        if (player.getRole() == null || !player.getRole().isBlack()) throw new SecurityException("Вы не в мафии!");

        switch (currentPhase) {
            case FIRST_NIGHT -> logMessage(player.getName(), message);
            case DAY -> logMessage(player.getName(), "(Скрытно) " + message);
            case NIGHT -> throw new IllegalStateException("В эту ночь мафия должна молчать! Общение запрещено.");
        }
    }

    public Player getPlayer(String id) { return players.get(id); }
    public List<Player> getAllPlayers() { return new ArrayList<>(players.values()); }
    private void logMessage(String senderName, String message) { mafiaChatLog.add(senderName + ": " + message); }
}

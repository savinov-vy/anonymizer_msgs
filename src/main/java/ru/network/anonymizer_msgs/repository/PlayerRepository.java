package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.handler.DayHandler;
import ru.network.anonymizer_msgs.handler.NightHandler;
import ru.network.anonymizer_msgs.model.AddPlayerModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.utils.NamesUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerRepository {

    UserRepository userRepository;

    private final Map<Long, Player> players = new ConcurrentHashMap<>();

    public void clear() {
        players.clear();
        //todo тут нужно дернуть Love38Bot чтобы перевести игроков в консистентное состояние
    }

    public List<Player> getSuicides(GamePhase gamePhase) {
        if (gamePhase == GamePhase.NIGHT) {
            Set<Long> shooters = NightHandler.shooterIdTargetId.keySet();
            Set<Long> notShooters = findAliveBlackPlayers().stream()
                    .filter(Player::isAlive)
                    .map(Player::getId)
                    .filter(id -> !shooters.contains(id))
                    .collect(Collectors.toSet());
            if (notShooters.isEmpty()) {
                return List.of();
            }

            return notShooters.stream()
                    .map(players::get)
                    .toList();
        } else if (gamePhase == GamePhase.DAY) {
            Set<Long> voters = DayHandler.voterIdTargetId.keySet();
            Set<Long> notVoters = players.values().stream()
                    .filter(Player::isAlive)
                    .map(Player::getId)
                    .filter(id -> !voters.contains(id))
                    .collect(Collectors.toSet());
            if (notVoters.isEmpty()) {
                return List.of();
            }
            return notVoters.stream()
                    .map(players::get)
                    .toList();
        }
        return List.of();
    }

    public String getResultVoters() {
        for (Map.Entry<Long, Long> voter : DayHandler.voterIdTargetId.entrySet()) {
            Long voterId = voter.getKey();
            Long targetId = voter.getValue();
            считаем за кого проголосовали и убиваем его
        }
    }

    public void kill(Long id) {
        Player player = players.get(id);
        player.setAlive(false);
    }

    public boolean contains(Long id) {
        return players.containsKey(id);
    }

    public Integer amountAll() {
        return players.size();
    }

    public Player findById(Long id) {
        return players.get(id);
    }

    public List<Player> getAll() {
        return new ArrayList<>(players.values());
    }

    public void setActionType(ActionType actionType) {
        for (Player player : players.values()) {
            player.setActionType(actionType);
        }
    }

    public boolean isRedMoreBlack() {
        List<Player> playersl = getAll();
        int black = 0;
        int red = 0;
        for (Player p : playersl) {
            if (p.isAlive()) {
                continue;
            }
            if (p.getRole() == Role.MAFIA || p.getRole() == Role.DON) {
                black++;
            } else if (p.getRole() == Role.CIVILIAN || p.getRole() == Role.SHERIFF) {
                red++;
            }
        }
        return red > black;
    }

    public List<Player> findAliveBlackPlayers() {
        return getAll().stream()
                .filter(p -> p.isAlive()
                        && (p.getRole() == Role.MAFIA || p.getRole() == Role.DON))
                .toList();
    }

    public boolean isDonSheriffAlive() {
        return isDonAlive() || isSheriffAlive();
    }

    /**
     * Есть ли среди живых хотя бы один игрок с ролью DON.
     */
    public boolean isDonAlive() {
        return players.values().stream()
                .anyMatch(p -> p.isAlive() && p.getRole() == Role.DON);
    }

    /**
     * Есть ли среди живых хотя бы один игрок с ролью SHERIFF.
     */
    public boolean isSheriffAlive() {
        return players.values().stream()
                .anyMatch(p -> p.isAlive() && p.getRole() == Role.SHERIFF);
    }

    /**
     * игрок с ролью DON.
     */
    public Player findDon() {
        return players.values().stream()
                .filter(p -> p.getRole() == Role.DON)
                .findFirst().orElse(null);
    }

    /**
     * игроки мафия
     */
    public List<Player> findMafia() {
        return players.values().stream()
                .filter(p -> p.getRole() == Role.MAFIA)
                .toList();
    }

    /**
     * Жители города
     */
    public List<Player> findRedPlayers() {
        return players.values().stream()
                .filter(p -> (p.getRole() == Role.CIVILIAN) || (p.getRole() == Role.SHERIFF))
                .toList();
    }

    public AddPlayerModel getOrCreatePlayer(Long userId) {
        Player player = null;
        if (!contains(userId) && (getAll().isEmpty() || isRegisteredProcess(getAll()))) {
            String newPlayerName = NamesUtils.getName(userRepository.findUserById(userId));
            player = Player.builder()
                    .id(userId)
                    .name(newPlayerName)
                    .role(null)
                    .isAlive(true)
                    .actionType(ActionType.REGISTERED)
                    .registeredAt(LocalDateTime.now())
                    .build();
            players.put(userId, player);
        } else if (contains(userId)) {
            player = players.get(userId);
        }
        return AddPlayerModel.builder()
                .added(player)
                .amount(amountAll())
                .build();
    }

    private boolean isRegisteredProcess(List<Player> players) {
        return players.stream()
                .allMatch(p -> p.getActionType() == ActionType.REGISTERED);
    }

}

package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.utils.NamesUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerRepository {

    UserRepository userRepository;

    private final Map<Long, Player> players = new ConcurrentHashMap<>();

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

    public List<Player> findBlack() {
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

    public Player addPlayer(Long userId) {
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
        }
        return player;
    }

    private boolean isRegisteredProcess(List<Player> players) {
        return players.stream()
                .allMatch(p -> p.getActionType() == ActionType.REGISTERED);
    }

}

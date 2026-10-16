package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;
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

    public Player addPlayer(Long userId) {
        Player player = null;
        if (!contains(userId) && (getAll().isEmpty() || isRegisteredProcess(getAll()))) {
            String newPlayerName = NamesUtils.getName(userRepository.findUserById(userId));
            player = Player.builder()
                    .id(userId)
                    .name(newPlayerName)
                    .role(null)
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

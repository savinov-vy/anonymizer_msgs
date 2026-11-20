package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.entity.User;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.utils.NamesUtils;

import java.time.LocalDateTime;
import java.util.*;
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

    public List<Player> getAll() {
        return new ArrayList<>(players.values());
    }

    public Player getOrCreatePlayer(Long userId) {
        Player player = null;
        if (!contains(userId)) {
            User userById = userRepository.findUserById(userId);
            Boolean isMan = Optional.ofNullable(userById)
                    .map(User::getIsMan)
                    .orElse(null);
            if (isMan == null) {
                return null;
            }
            String newPlayerName = NamesUtils.getName(userById);
            player = Player.builder()
                    .id(userId)
                    .name(newPlayerName)
                    .banEndAt(LocalDateTime.now().minusDays(1))
                    .registeredAt(LocalDateTime.now())
                    .build();
            players.put(userId, player);
        } else if (contains(userId)) {
            player = players.get(userId);
        }
        return player;
    }

}

package ru.network.anonymizer_msgs.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.controller.WebHookBot;
import ru.network.anonymizer_msgs.entity.User;
import ru.network.anonymizer_msgs.handler.DayHandler;
import ru.network.anonymizer_msgs.handler.NightHandler;
import ru.network.anonymizer_msgs.model.AddPlayerModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.service.SenderService;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.utils.NamesUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerRepository {

    UserRepository userRepository;

    private final Map<Long, Player> players = new ConcurrentHashMap<>();
    private final SenderService senderService;

    public void clear() {
        players.clear();
        //todo тут нужно дернуть Love38Bot чтобы перевести игроков в консистентное состояние
    }
    private static final List<String> VERDICTS = List.of(
            "\uD83D\uDC80 %s казнён.",
            "⚰\uFE0F Город вынес приговор: %s казнён.",
            "\uD83E\uDE78 %s вздёрнут на виселице.",
            "\uD83D\uDC80 %s отправлен на эшафот",
            "\uD83E\uDEA6 %s казнён. Город не прощает."
    );

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
        StringBuilder report = new StringBuilder();
        report.append("\uD83E\uDE78 Голосование окончено. Город назвал имена.\n\n");
        Map<Long, Integer> result = new HashMap<>();
        for (Map.Entry<Long, Long> voter : DayHandler.voterIdTargetId.entrySet()) {
            Long voterId = voter.getKey();
            Long targetId = voter.getValue();
            String voterName = players.get(voterId).getName();
            String targetName = players.get(targetId).getName();
            report.append(String.format("\uD83D\uDD2A %s проголосовал против %s.\n", voterName, targetName));
            result.merge(targetId, 1, Integer::sum);
        }
        report.append("\n⚖\uFE0F Счёт:\n");
        Long targetUserId = null;
        Integer maxVote = 0;
        for (Map.Entry<Long, Integer> resultEntry : result.entrySet()) {
            Long userId = resultEntry.getKey();
            Integer amountVote = resultEntry.getValue();
            String playerName = players.get(userId).getName();
            report.append(playerName + " - " + amountVote);
            targetUserId = maxVote < amountVote ? userId : targetUserId;
        }
        report.append(String.format(getRandom(VERDICTS), players.get(targetUserId).getName()));
        report.append("\uD83C\uDF11 Ночь близко...");
        kill(targetUserId);
        return report.toString();
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

    public List<Long> findAliveBlackPlayerIds() {
        return getAll().stream()
                .filter(p -> p.isAlive()
                        && (p.getRole() == Role.MAFIA || p.getRole() == Role.DON))
                .map(Player::getId)
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
        if (!contains(userId) && isRegisteredProcess(getAll())) {
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
                    .role(null)
                    .isAlive(true)
                    .actionType(ActionType.REGISTERED)
                    .registeredAt(LocalDateTime.now())
                    .build();
            players.put(userId, player);
            senderService.sendMafiaMessage("Игрок " + newPlayerName + " присоединился! Всего игроков: " + players.size() + "/10");
            WebHookBot.isSendStartFirstNight.set(false);
        } else if (contains(userId)) {
            player = players.get(userId);
        }
        return AddPlayerModel.builder()
                .added(player)
                .amount(amountAll())
                .build();
    }

    private boolean isRegisteredProcess(List<Player> players) {
        if (players.isEmpty()) {
            return true;
        }
        //todo оставляем пока регистред, потом можно будет поставить size 10 регистеред универсален
        return players.stream()
                .allMatch(p -> p.getActionType() == ActionType.REGISTERED);
    }

    private static String getRandom(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(texts.size());
        return texts.get(index);
    }
}

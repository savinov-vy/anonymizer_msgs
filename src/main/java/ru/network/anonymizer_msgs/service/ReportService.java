package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.handler.DayHandler;
import ru.network.anonymizer_msgs.handler.NightHandler;
import ru.network.anonymizer_msgs.model.AddPlayerModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.repository.UserRepository;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;
import ru.network.anonymizer_msgs.utils.NamesUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static ru.network.anonymizer_msgs.type.GamePhase.DAY;
import static ru.network.anonymizer_msgs.type.GamePhase.FIRST_DAY;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReportService {

    PlayerRepository playerRepository;
    SenderService senderService;

    private static final String ONE_SUICIDE_NIGHT_MSG = "\uD83D\uDD6F\uFE0F Ночь забрала душу.\n" +
            "Пока город спал, %s в одиночестве принял(-а) роковое решение. " +
            "Утром тело нашли без признаков борьбы — это было самоубийство.\n" +
            "Тишина повисла над улицами...\n";

    private static final String MANY_SUICIDE_NIGHT_MSG = "\uD83C\uDF19 Ночь опустилась на город...\n" +
            "Этой ночью несколько из жителей не смогли справиться с отчаянием и свёли счёты с жизнью.\n" +
            "%s найдены мёртвыми на рассвете. Причина — самоубийство.\n" +
            "Город в шоке. Кто следующий?";

    private static final String ONE_SUICIDE_DAY_MSG = "\u2600\uFE0F День не принёс облегчения.\n" +
            "Среди бела дня %s в одиночестве принял(-а) роковое решение. " +
            "Тело нашли без признаков борьбы — это было самоубийство.\n" +
            "Город замер в молчании...\n";

    private static final String MANY_SUICIDE_DAY_MSG = "\uD83C\uDF24\uFE0F День омрачил город...\n" +
            "Сегодня несколько из жителей не смогли справиться с отчаянием и свели счёты с жизнью.\n" +
            "%s найдены мёртвыми. Причина — самоубийство.\n" +
            "Город в шоке. Кто следующий?";

    private static final List<String> VERDICTS = List.of(
            "\uD83D\uDC80 %s казнён.",
            "⚰\uFE0F Город вынес приговор: %s казнён.",
            "\uD83E\uDE78 %s вздёрнут на виселице.",
            "\uD83D\uDC80 %s отправлен на эшафот",
            "\uD83E\uDEA6 %s казнён. Город не прощает."
    );


    public String getSuicidesReportDay() {
        List<Player> suicides = getSuicidesDay();
        suicides.stream()
                .map(Player::getId)
                .forEach(playerRepository::kill);
        String names = names(suicides);
        if (suicides != null && suicides.size() == 1) {
            return String.format(ONE_SUICIDE_DAY_MSG, names);
        } else if (suicides != null && suicides.size() > 1) {
            return String.format(MANY_SUICIDE_DAY_MSG, names);
        }
        return "";
    }

    public String getSuicidesReportNight() {
        List<Player> suicides = getSuicidesNight();
        suicides.stream()
                .map(Player::getId)
                .forEach(playerRepository::kill);
        String names = names(suicides);
        if (suicides != null && suicides.size() == 1) {
            return String.format(ONE_SUICIDE_NIGHT_MSG, names);
        } else if (suicides != null && suicides.size() > 1) {
            return String.format(MANY_SUICIDE_NIGHT_MSG, names);
        }
        return "";
    }


    private List<Player> getSuicidesNight() {
        Set<Long> shooters = NightHandler.shooterIdTargetId.keySet();
        Set<Long> notShooters = playerRepository.findAliveBlackPlayers().stream()
                .filter(Player::isAlive)
                .map(Player::getId)
                .filter(id -> !shooters.contains(id))
                .collect(Collectors.toSet());
        if (notShooters.isEmpty()) {
            return List.of();
        }

        return notShooters.stream()
                .map(playerRepository::findById)
                .toList();
    }

    public List<Player> getSuicidesDay() {
        Set<Long> voters = DayHandler.voterIdTargetId.keySet();
        Set<Long> notVoters = playerRepository.getAll().stream()
                .filter(Player::isAlive)
                .map(Player::getId)
                .filter(id -> !voters.contains(id))
                .collect(Collectors.toSet());
        if (notVoters.isEmpty()) {
            return List.of();
        }
        return notVoters.stream()
                .map(playerRepository::findById)
                .toList();
    }

    public String sendResultReportNight() {
        StringBuilder report = new StringBuilder();

    }

    public void sendResultReportDay() {
        StringBuilder report = new StringBuilder();
        report.append("\uD83E\uDE78 Голосование окончено. Город назвал имена.\n\n");
        Map<Long, Integer> result = new HashMap<>();
        for (Map.Entry<Long, Long> voter : DayHandler.voterIdTargetId.entrySet()) {
            Long voterId = voter.getKey();
            Long targetId = voter.getValue();
            String voterName = playerRepository.findById(voterId).getName();
            String targetName = playerRepository.findById(targetId).getName();
            report.append(String.format("\uD83D\uDD2A %s проголосовал против %s.\n", voterName, targetName));
            result.merge(targetId, 1, Integer::sum);
        }
        report.append("\n⚖\uFE0F Счёт:\n");
        Long targetUserId = null;
        Integer maxVote = 0;
        for (Map.Entry<Long, Integer> resultEntry : result.entrySet()) {
            Long userId = resultEntry.getKey();
            Integer amountVote = resultEntry.getValue();
            String playerName = playerRepository.findById(userId).getName();
            report.append(playerName + " - " + amountVote);
            targetUserId = maxVote < amountVote ? userId : targetUserId;
        }
        report.append(String.format(getRandom(VERDICTS), playerRepository.findById(targetUserId).getName()));
        report.append("\n\n");
        report.append(getSuicidesReportDay());
        report.append("\uD83C\uDF11 Ночь близко...");
        kill(targetUserId);
        senderService.sendMafiaMessage(report.toString());
    }

    public void kill(Long id) {
        Player player = playerRepository.findById(id);
        player.setAlive(false);
    }


    private static String getRandom(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(texts.size());
        return texts.get(index);
    }


    private String names(List<Player> players) {
        if (players == null || players.size() == 0) {
            return "";
        }
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }
}

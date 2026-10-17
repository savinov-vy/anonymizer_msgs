package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.handler.DayHandler;
import ru.network.anonymizer_msgs.handler.NightHandler;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;


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

    private static final List<String> MAFIA_REPORT_MISTAKE = List.of(
            "\uD83C\uDF19 Город затих. \uD83D\uDEAA Дверь отворилась — и закрылась. \uD83D\uDC63 Шаги ушли в темноту.\n" +
                    "\uD83D\uDD78\uFE0F Мафия искала. Но не нашла. \uD83E\uDEC0\n" +
                    "\uD83C\uDF05 Утро наступило для всех. Пока что.",
            "\uD83D\uDD2A Мафия сделала свой выбор. \uD83E\uDE78 Но клинок рассек только воздух.\n" +
                    "\uD83D\uDD77\uFE0F Кто-то проснулся. И не понял, что был на волоске. ⚰\uFE0F\n" +
                    "\uD83C\uDF11 Мафия ошиблась. В этот раз.",
            "❄\uFE0F Холод пробрался в дом. \uD83E\uDDCA Холод остановился у чьей-то кровати. И прошёл мимо.\n" +
                    "\uD83E\uDEA6 Кто-то не проснётся. Но не сегодня. \uD83D\uDD6F\uFE0F\n" +
                    "\uD83C\uDF05 Утро. Все живы. Пока что.",
            "\uD83C\uDF11 Тень скользнула по стене. \uD83D\uDD73\uFE0F Тень замерла. И исчезла.\n" +
                    "\uD83D\uDD78\uFE0F Мафия не оставляет свидетелей. Но иногда — оставляет. \uD83E\uDEC0\n" +
                    "\uD83C\uDF05 Утро. Все дышат. Пока что.",
            "\uD83D\uDD2A Мафия промахнулась. \uD83E\uDE78\n" +
                    "\uD83D\uDD77\uFE0F Кто-то жив. Пока что. ⚰\uFE0F\n" +
                    "\uD83C\uDF11",
            "\uD83E\uDE9E Зеркало треснуло. \uD83D\uDD73\uFE0F В отражении — чьё-то лицо. Живое.\n" +
                    "\uD83D\uDD78\uFE0F Мафия смотрела. Мафия ушла. \uD83E\uDEC0\n" +
                    "\uD83C\uDF05 Но она вернётся. ⚰\uFE0F",
            "\uD83D\uDEAA Дверь скрипнула. \uD83D\uDD6F\uFE0F Свеча дрогнула. Тень легла на чьё-то лицо — и отступила.\n" +
                    "\uD83E\uDE78 Мафия не промахнулась. Мафия передумала. \uD83D\uDD77\uFE0F\n" +
                    "\uD83C\uDF11 Или просто ждала. ⚰\uFE0F",
            "\uD83C\uDF19 Ночь прошла. \uD83D\uDD2A Мафия вышла на охоту. \uD83E\uDE78 Но вернулась с пустыми руками.\n" +
                    "\uD83D\uDD78\uFE0F Город не потерял никого. \uD83E\uDEC0\n" +
                    "\uD83C\uDF05 Пока что. ⚰\uFE0F",
            "\uD83C\uDF05 Утро. Все проснулись. \uD83D\uDD6F\uFE0F Все дышат.\n" +
                    "\uD83D\uDD77\uFE0F Но мафия где-то рядом. И она запомнила. \uD83E\uDE78\n" +
                    "\uD83C\uDF11 Следующей ночью она не промахнётся. ⚰\uFE0F",
            "\uD83D\uDD2A Промах. \uD83E\uDE78\n" +
                    "\uD83E\uDEC0 Кто-то жив. ⚰\uFE0F\n" +
                    "\uD83C\uDF05 Пока что."
    );

    private static final List<String> MAFIA_REPORT_TARGET = List.of(
            "\uD83D\uDD6F\uFE0F Свеча догорела. В коридоре послышались шаги. Они замерли у двери %s.\n" +
                    "\uD83E\uDE78 Он не проснётся. \uD83D\uDC80 \n",
            "\uD83C\uDF11 Город затих. Где-то скрипнула половица. Скрип оборвался у кровати %s.\n" +
                    "\uD83E\uDE78 Один вздох. И тишина. \uD83D\uDC80",
            "\uD83D\uDD6F\uFE0F Пламя дрогнуло. Дверь отворилась без звука. За ней — только темнота. И %s.\n" +
                    "\uD83E\uDE78 Больше он не проснётся. \uD83D\uDC80",
            "\uD83D\uDEAA Дверь отворилась без звука. \uD83D\uDC63 Шаги замерли у кровати %s.\n" +
                    "\uD83E\uDE78 Он не проснётся. \uD83D\uDD78\uFE0F",
            "\uD83C\uDF19 Город затих. \uD83E\uDEB5 Где-то скрипнула половица. Скрип оборвался у %s.\n" +
                    "\uD83E\uDEC0 Один вздох. И тишина. ⚰\uFE0F",
            "\uD83C\uDF43 Ветер хлопнул ставней. \uD83C\uDF2C\uFE0F Холод пробрался в дом. Холод остановился у %s.\n" +
                    "\uD83D\uDD6F\uFE0F Утро для него не наступит. \uD83E\uDEA6",
            "\uD83E\uDD2B Тишина стала густой, как смола. \uD83D\uDD73\uFE0F В этой тишине растворилось имя — %s.\n" +
                    "\uD83D\uDD77\uFE0F Мафия не оставляет свидетелей. \uD83E\uDE78",
            "⏰ Часы пробили полночь. \uD83D\uDD5B Последний удар пришёлся на имя %s.\n" +
                    "\uD83E\uDEA6 Он не проснётся. \uD83C\uDF11",
            "❄\uFE0F Ночь опустилась на город. Холод пробрался в дом. \uD83E\uDDCA Холод остановился у %s.\n" +
                    "\uD83D\uDD78\uFE0F Утром его не будет. ⚰\uFE0F",
            "\uD83D\uDD14 Где-то звякнуло стекло. \uD83D\uDD07 Звук оборвался там, где спал %s.\n" +
                    "\uD83E\uDE78 Мафия сделала свой выбор. \uD83D\uDD77\uFE0F",
            "\uD83C\uDF03 Город затих. \uD83C\uDF2B\uFE0F Что-то шевельнулось в темноте. Что-то нашло %s.\n" +
                    "\uD83E\uDEC0 Он не проснётся. \uD83E\uDEA6",
            "\uD83D\uDC93 Город спит. Только сердце стучит. \uD83D\uDC94 Стук оборвался. Это было сердце %s.\n" +
                    "\uD83D\uDD78\uFE0F Он уже не дышит. ⚰\uFE0F",
            "\uD83D\uDEAA Дверь отворилась. За ней — только темнота. \uD83C\uDF11 Темнота вошла и осталась у %s.\n" +
                    "\uD83D\uDD77\uFE0F Больше он не проснётся. \uD83E\uDEA6\n",
            "\uD83D\uDD78\uFE0F Что-то коснулось стены. \uD83E\uDEA6 Что-то коснулось %s.\n" +
                    "\uD83E\uDEC0 Он не проснётся. \uD83C\uDF11",
            "\uD83E\uDE9E Зеркало треснуло. \uD83D\uDD73\uFE0F В отражении мелькнуло %s.\n" +
                    "\uD83E\uDE78 Больше он не проснётся. ⚰\uFE0F"
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

    public void sendResultReportNight() {
        boolean isEqualsTarget = true;
        Long lastTargetId = null;
        StringBuilder reportToMafiaPrivate = new StringBuilder();
        reportToMafiaPrivate.append("\uD83E\uDE78 Доклад мафии.");
        Long targetId = null;
        for (Map.Entry<Long, Long> voter : NightHandler.shooterIdTargetId.entrySet()) {
            Long voterId = voter.getKey();
            targetId = voter.getValue();
            reportToMafiaPrivate.append(String.format("\uD83D\uDD2A %s → %s.",
                    playerRepository.findById(voterId).getName(),
                    playerRepository.findById(targetId).getName()));
            if (lastTargetId == null) {
                lastTargetId = targetId;
                isEqualsTarget = true;
            } else {
                isEqualsTarget = isEqualsTarget && (lastTargetId.equals(targetId));
                lastTargetId = targetId;
            }
        }

        String toMafiaChatReport = isEqualsTarget
                ? String.format(getRandom(MAFIA_REPORT_TARGET), playerRepository.findById(lastTargetId))
                : getRandom(MAFIA_REPORT_MISTAKE);

        if (isEqualsTarget) {
            playerRepository.kill(targetId);
        } else {
            reportToMafiaPrivate.append("💀 Оба живы. И даже не знают, как близко была смерть.\n" +
                    "⚠️ Напоминание: чтобы убить, стреляйте в одного и того же игрока.\n" +
                    "🌑 Убийство в мафии — это настоящее искусство.");
            senderService.sendNowPrivate(reportToMafiaPrivate.toString(), playerRepository.findAliveBlackPlayers());
        }

        String suicidesReportNight = getSuicidesReportNight();
        if (hasText(suicidesReportNight)) {
            toMafiaChatReport = toMafiaChatReport + "\n\n" + suicidesReportNight;
        }
        senderService.sendMafiaMessage(toMafiaChatReport);
        NightHandler.shooterIdTargetId.clear();
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
            report.append(playerName + " - \uD83D\uDC80" + amountVote);
            targetUserId = maxVote < amountVote ? userId : targetUserId;
        }
        report.append(String.format(getRandom(VERDICTS), playerRepository.findById(targetUserId).getName()));
        report.append("\n\n");
        String suicidesReportDay = getSuicidesReportDay();
        if (hasText(suicidesReportDay)) {
            report.append("\n\n");
            report.append(suicidesReportDay);
        }
        report.append("\uD83C\uDF11 Ночь близко...");
        kill(targetUserId);
        senderService.sendMafiaMessage(report.toString());
        DayHandler.voterIdTargetId.clear();
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

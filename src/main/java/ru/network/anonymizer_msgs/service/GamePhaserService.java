
package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.GamePhase;
import ru.network.anonymizer_msgs.type.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.network.anonymizer_msgs.type.ActionType.DON_CHIFF_NIGHT;
import static ru.network.anonymizer_msgs.type.GamePhase.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamePhaserService {

    PlayerRepository playerRepository;
    ReportService reportService;
    SenderService senderService;
    AtomicInteger timer = new AtomicInteger(0);
    AtomicInteger lastMessageId = new AtomicInteger(0);
    GamePlayerService gamePlayerService;
    MafiaClientService mafiaClientService;
    private static volatile GamePhase currentPhase;
    private static final Integer SECOND_60 = 60;
    private static final Integer SECOND_30 = 30;
    private static final Integer SECOND_120 = 120;
    private static final String FIRST_NIGHT_MSG = "🌌 Наступила ПЕРВАЯ НОЧЬ. У Мафии есть %s секунд на договорку в чате t.me/Love38Bot";
    private static final String FIRST_DAY_MSG = "🌅 Наступило ПЕРВОЕ УТРО. Ночь была тихой — никто не пострадал.\n\n" +
            "Начинаем первый круг: у всех %s секунд общения.\n" +
            "Голосование сегодня не проводим — информации пока нет.";
    private static final String NIGHT_MSG = "🌌 НОЧЬ. Город засыпает.\n\n" +
            "\uD83E\uDD29 Мафия — выберите жертву.\n" +
            "У вас %s секунд. Выбор — кнопками.";
    private static final String DON_SHERIF_NIGHT_MSG = "\uD83C\uDF19 Город погрузился в сон...\n\n" +
            "Но двоим сегодня не спится:\n" +
            "🎩 Дон вышел на ночную проверку.\n" +
            "🕵️ Шериф вышел на ночную проверку.\n\n" +
            "%s секунд. Выбор — кнопками.";
    private static final String DAY_MSG = "☀\uFE0F Город просыпается. Ночь была неспокойной...\n\n" +
            "💀 Жертва мафии: %s.\n\n" +
            "Город, ваше слово. Обсуждаем и голосуем кто виноват. Выбор кнопками.";

    private static final String MAFIA_WIN = "\uD83C\uDFA9 МАФИЯ ВЫИГРАЛА!\n\n%s";
    private static final String CIVILIAN_WIN = "\uD83D\uDC64 ЖИТЕЛИ ВЫИГРАЛИ!\n\n%s";

    /**
     * Автоматический запуск игры при наборе 10 человек
     */
    public synchronized void startFirstNight() {
        playerRepository.setActionType(ActionType.FIRST_NIGHT);
        List<Player> players = getRandomRoles();
        gamePlayerService.createNewGame(players);
        mafiaClientService.sendStartGame();
        sleep();
        setTimer(-100);
        currentPhase = GamePhase.FIRST_NIGHT;
    }

    public List<Player> getRandomRoles() {
        // 1. Формируем пул из 10 спортивных ролей
        List<Role> rolePool = new ArrayList<>(List.of(
                Role.DON, Role.MAFIA, Role.MAFIA, // 3 черных
                Role.SHERIFF,                     // 1 шериф
                Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN,
                Role.CIVILIAN, Role.CIVILIAN, Role.CIVILIAN // 6 мирных
        ));
        Collections.shuffle(rolePool);
        int i = 0;
        List<Player> players = playerRepository.getAll();
        for (Player player : players) {
            player.setRole(rolePool.get(i++));
        }
        return players;
    }

    public synchronized void managePhase() {
        if (currentPhase == null) {
            return;
        }

        Integer messageId = null;
        if (currentPhase == GamePhase.FIRST_NIGHT) {
            if (isFirstTakt()) {
                setTimer(SECOND_60);
                senderService.sendFirstNightPrivate();
                senderService.sendMafiaMessage(String.format(FIRST_NIGHT_MSG, SECOND_60)).orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(FIRST_NIGHT_MSG, second), lastMessageId.get())
                        .orElse(null);
                checkChangePhase();
            }
        } else if (currentPhase == FIRST_DAY) {
            if (isFirstTakt()) {
                setTimer(SECOND_60);
                messageId = senderService.sendMafiaMessage(String.format(FIRST_DAY_MSG, SECOND_60))
                        .orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(FIRST_DAY_MSG, second), lastMessageId.get())
                        .orElse(null);
                checkChangePhase();
            }
        } else if (currentPhase == NIGHT) {
            if (isFirstTakt()) {
                setTimer(SECOND_30);
                messageId = senderService.sendMafiaMessage(String.format(NIGHT_MSG, SECOND_30))
                        .orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(NIGHT_MSG, second), lastMessageId.get())
                        .orElse(null);
                checkChangePhase();
            }
        } else if (currentPhase == DON_SHERIFF_NIGHT) {
            if (isFirstTakt()) {
                setTimer(SECOND_30);
                messageId = senderService.sendMafiaMessage(String.format(DON_SHERIF_NIGHT_MSG, SECOND_30))
                        .orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(DON_SHERIF_NIGHT_MSG, second), lastMessageId.get())
                        .orElse(null);
                checkChangePhase();
            }
        } else if (currentPhase == DAY) {
            if (isFirstTakt()) {
                setTimer(SECOND_120);
                messageId = senderService.sendMafiaMessage(String.format(DAY_MSG, SECOND_120))
                        .orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(DAY_MSG, second), lastMessageId.get())
                        .orElse(null);
                if (timer.get() <= 0) {
                    senderService.sendMafiaMessage(playerRepository.getResultVoters());
                }
                checkChangePhase();
            }
        } else if (currentPhase == FINISHED) {
            String header = playerRepository.isRedMoreBlack() ? CIVILIAN_WIN : MAFIA_WIN;
            List<Player> players = playerRepository.getAll();
            String report = players.stream()
                    .map(p -> isAliveEmoji(p.isAlive()) + p.getName() + ": " + p.getRole())
                    .collect(Collectors.joining("\n"));
            String notification = "\n\n до следующей регистрации: %s";
            String toSend = header + report + notification;
            if (isFirstTakt()) {
                setTimer(SECOND_60);
                messageId = senderService.sendMafiaMessage(String.format(toSend, SECOND_60)).orElse(null);
                lastMessageId.set(messageId);
            } else {
                int seconds = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(toSend, seconds), lastMessageId.get());
                if (seconds <= 0) {
                    playerRepository.clear();
                    currentPhase = null;
                }
            }

        }
        checkEndGame();
    }

    private String names(List<Player> players) {
        if (players == null || players.size() == 0) {
            return "";
        }
        return players.stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
    }

    private void checkEndGame() {
        if (!playerRepository.isRedMoreBlack()) {
            gameOver();
        }
    }

    private void checkChangePhase() {
        if (timer.get() <= 0) {
            changePhase();
            timer.set(-100);
        }
        decrement();
    }

    private synchronized void changePhase() {
        switch (currentPhase) {
            case FIRST_NIGHT -> {
                playerRepository.setActionType(ActionType.FIRST_DAY);
                currentPhase = FIRST_DAY;
            }
            case FIRST_DAY -> {
                playerRepository.setActionType(ActionType.NIGHT);
                currentPhase = NIGHT;
            }
            case NIGHT -> {
                reportService.sendResultReportNight();
                playerRepository.setActionType(playerRepository.isDonSheriffAlive() ? DON_CHIFF_NIGHT : ActionType.DAY);
                currentPhase = playerRepository.isDonSheriffAlive() ? DON_SHERIFF_NIGHT : DAY;
            }
            case DAY -> {
                reportService.sendResultReportDay();
                playerRepository.setActionType(ActionType.NIGHT);
                currentPhase = NIGHT;
            }
            case DON_SHERIFF_NIGHT -> {
                playerRepository.setActionType(ActionType.DAY);
                currentPhase = DAY;
            }
        }
    }

    private boolean isFirstTakt() {
        return timer.get() < -50;
    }

    /**
     * Отчет по окончанию игры
     */
    private void gameOver() {
        currentPhase = FINISHED;
        setTimer(-100);
    }

    public void decrement() {
        timer.decrementAndGet();
    }

    public synchronized void setTimer(int newVal) {
        timer.set(newVal);
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }

    private String isAliveEmoji(boolean isAlive) {
        return isAlive ? "\uD83D\uDC9A" : "☠\uFE0F";
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void pulse() {
        managePhase();
    }

}

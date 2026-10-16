
package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.checkerframework.checker.units.qual.N;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.MediaMsgModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.GamePhase;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.network.anonymizer_msgs.type.ActionType.DON_CHIFF_NIGHT;
import static ru.network.anonymizer_msgs.type.GamePhase.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamePhaserService {

    private final PlayerRepository playerRepository;
    private final SenderService senderService;
    private final AtomicInteger timer = new AtomicInteger(0);
    private final AtomicInteger lastMessageId = new AtomicInteger(0);
    private static GamePhase currentPhase;
    private static final Integer SECOND_60 = 60;
    private static final Integer SECOND_30 = 30;
    private static final Integer SECOND_180 = 180;
    private static final String FIRST_NIGHT_MSG = "🌌 Наступила ПЕРВАЯ НОЧЬ. У Мафии есть %s секунд на договорку в чате t.me/Love38Bot";
    private static final String PRIVATE_FIRST_NIGHT_MSG = "\uD83C\uDFA9 ВЫ — МАФИЯ. Договоритесь о порядке отстрела. И кодовых словах. У вас 60 секунд." +
            "\n\nБосс Мафии: %s" +
            "\nМафия: %s" +
            "\n\nЖители города: %s";
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


    public void decrement() {
        timer.decrementAndGet();
    }

    public void setTimer(int newVal) {
        timer.set(newVal);
    }

    public void managePhase() {
        if (currentPhase == null) {
            return;
        }

        Integer messageId = null;
        if (currentPhase == GamePhase.FIRST_NIGHT) {
            if (timer.get() < -50) {
                setTimer(SECOND_60);
                String donName = playerRepository.findDon().getName();
                List<Player> mafia = playerRepository.findMafia();
                List<Player> redPlayers = playerRepository.findRedPlayers();

                senderService.sendNowPrivate(String.format(PRIVATE_FIRST_NIGHT_MSG, donName,
                        names(mafia), names(redPlayers)), playerRepository.getAll());
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
                setTimer(SECOND_180);
                messageId = senderService.sendMafiaMessage(String.format(DAY_MSG, SECOND_180))
                        .orElse(null);
                lastMessageId.set(messageId);
            } else {
                int second = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(DAY_MSG, second), lastMessageId.get())
                        .orElse(null);
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
                messageId = senderService.sendMafiaMessage(String.format(toSend, 60)).orElse(null);
            } else {
                int seconds = timer.decrementAndGet();
                senderService.editMafiaMessage(String.format(toSend, seconds), messageId);
                if (seconds <= 0) {
                    currentPhase = null;
                    playerRepository.clear();
                }
            }

        }
        checkEndGame();
    }

    private String names(List<Player> players) {
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

    private void changePhase() {
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
                playerRepository.setActionType(playerRepository.isDonSheriffAlive() ? DON_CHIFF_NIGHT : ActionType.DAY);
                currentPhase = playerRepository.isDonSheriffAlive() ? DON_SHERIFF_NIGHT : DAY;
            }
            case DAY -> {
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
     * Автоматический запуск игры при наборе 10 человек
     */
    public void startFirstNight() {
        currentPhase = GamePhase.FIRST_NIGHT;
        setTimer(-100);
    }

    /**
     * Отчет по окончанию игры
     */
    public void gameOver() {
        currentPhase = FINISHED;
        setTimer(-100);
    }

    private String isAliveEmoji(boolean isAlive) {
        return isAlive ? "\uD83D\uDC9A" : "☠\uFE0F";
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void pulse() {
        managePhase();
    }

}

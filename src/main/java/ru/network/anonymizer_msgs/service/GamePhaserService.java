
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
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.type.GamePhase;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.network.anonymizer_msgs.type.GamePhase.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamePhaserService {

    private static GamePhase currentPhase;
    private final SenderService senderService;
    private final AtomicInteger timer = new AtomicInteger(0);
    private final AtomicInteger lastMessageId = new AtomicInteger(0);
    private static final Integer SECOND_60 = 60;
    private static final Integer SECOND_30 = 30;
    private static final Integer SECOND_180 = 180;
    private static final String FIRST_NIGHT_MSG = "🌌 Наступила ПЕРВАЯ НОЧЬ. У Мафии есть %s секунд на договорку в чате t.me/Love38Bot";
    private static final String PRIVATE_FIRST_NIGHT_MSG = "";
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
    private final PlayerRepository playerRepository;


    public void decrement() {
        timer.decrementAndGet();
    }

    public void minus(int delta) {
        timer.addAndGet(-delta);
    }

    public void setTimer(int newVal) {
        timer.set(newVal);
    }

    public void changePhase(GamePhase newPhase) {
//        this.currentPhase = newPhase;
//
//        if (newPhase == GamePhase.DAY) {
//            notifyAllAsync("☀️ Наступил ДЕНЬ! Город просыпается.");
//
//            // ДОБАВИТЬ: Через 3 минуты день автоматически закроется и наступит ночь
//            taskScheduler.schedule(
//                    () -> changePhase(GamePhase.NIGHT),
//                    Instant.now().plusSeconds(180)
//            );
//
//        } else if (newPhase == GamePhase.NIGHT) {
//            notifyAllAsync(" Наступила НОЧЬ! Чат мафии заблокирован.");
//
//            taskScheduler.schedule(
//                    () -> changePhase(GamePhase.DAY),
//                    Instant.now().plusSeconds(45)
//            );
//        }
    }

    public void managePhase() {
        if (currentPhase == null) {
            return;
        }

        Integer messageId = null;
        if (currentPhase == GamePhase.FIRST_NIGHT) {
            if (timer.get() < -50) {
                setTimer(SECOND_60);
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
        }
        checkEndGame();
    }

    private void checkEndGame() {
        // проверить остались ли красные и черные
        // если конец gameOver()
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
            case FIRST_NIGHT -> currentPhase = FIRST_DAY;
            case FIRST_DAY -> currentPhase = NIGHT;
            case NIGHT -> {
                currentPhase = playerRepository.isDonSheriffAlive() ? DON_SHERIFF_NIGHT : DAY;
            }
            case DAY -> currentPhase = NIGHT;
            case DON_SHERIFF_NIGHT -> currentPhase = DAY;
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
     * Автоматический запуск игры при наборе 10 человек
     */
    public void gameOver() {
        currentPhase = null;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void pulse() {
        managePhase();
    }

}

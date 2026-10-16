
package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
import ru.network.anonymizer_msgs.type.GamePhase;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GamePhaserService {

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
//            notifyAllAsync("🌙 Наступила НОЧЬ! Чат мафии заблокирован.");
//
//            taskScheduler.schedule(
//                    () -> changePhase(GamePhase.DAY),
//                    Instant.now().plusSeconds(45)
//            );
//        }
    }

    /**
     * Автоматический запуск игры при наборе 10 человек
     */
    private void startFirstNight() {
//        this.currentPhase = GamePhase.FIRST_NIGHT;
//        mafiaChatLog.clear();
//
//        notifyAllAsync("🌌 Наступила ПЕРВАЯ НОЧЬ. У Мафии есть 60 секунд на договорку в чате бота!");
//
//        // 3. Таймер на 60 секунд до наступления дня
//        taskScheduler.schedule(
//                () -> changePhase(GamePhase.DAY),
//                Instant.now().plusSeconds(60)
//        );
    }

}

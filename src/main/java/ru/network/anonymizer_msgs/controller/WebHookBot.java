package ru.network.anonymizer_msgs.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.AddPlayerModel;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.repository.UserRepository;
import ru.network.anonymizer_msgs.service.GamePhaserService;
import ru.network.anonymizer_msgs.service.SenderService;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebHookBot {

    PlayerRepository playerRepository;
    MainController mainController;
    SenderService senderService;
    GamePhaserService gamePhaserService;
    public static AtomicBoolean isSendStartFirstNight = new AtomicBoolean(false);
    private static final Integer COMPLETE_GAMERS = 10;

    public void handleUpdate(Update update) {
        Long userId = extractUserId(update);
        if (userId == null) {
            return;
        }
        AddPlayerModel current = playerRepository.getOrCreatePlayer(userId);
        if (current.getAdded() == null) {
            senderService.deleteMessage(update);
        } else if (COMPLETE_GAMERS.equals(current.getAmount()) && !isSendStartFirstNight.get()) {
            isSendStartFirstNight.set(true);
            gamePhaserService.startFirstNight();
            return;
        }
        mainController.distribute(update, current.getAdded());
    }

    private Long extractUserId(Update update) {
        org.telegram.telegrambots.meta.api.objects.User from = null;

        if (update.hasMessage()) {
            from = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            from = update.getCallbackQuery().getFrom();
        } else if (update.getMyChatMember() != null) {
            from = update.getMyChatMember().getFrom();
        }

        if (from != null) {
            return from.getId();
        }

        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        return null;
    }
}

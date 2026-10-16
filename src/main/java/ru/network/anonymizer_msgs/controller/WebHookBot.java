package ru.network.anonymizer_msgs.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.service.SenderService;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebHookBot {

    PlayerRepository playerRepository;
    MainController mainController;
    SenderService senderService;

    public void handleUpdate(Update update) {
        Long userId = extractUserId(update);
        if (userId == null) {
            return;
        }

        Player added = playerRepository.addPlayer(userId);
        if (added == null) {
            //todo отправить в личку Love38Bot, а тут сообщение удалить
//            "Извините, игра уже началась (10/10)."
//            senderService.sendNow();
        }
        mainController.distribute(update, added);
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


package ru.network.anonymizer_msgs.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.MediaMsgModel;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SenderService implements AdminNotifier {

    TelegramClient telegramClient;

    public void sentMessage(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(chatId, text, null);
        sendNow(sendMessage);
    }

    public void sendHTMLMessage(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getHTMLSendMessage(chatId, text, null);
        sendNow(sendMessage);
    }

    public void sendRedirectableMsg(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessageRedirectable(chatId, text, null);
        sendNow(sendMessage);
    }

    public void sentMessage(String text, String channelName) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(channelName, text, null);
        sendNow(sendMessage);
    }

    public void sentMessage(String text, Long chatId, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(chatId, text, keyboardMarkup);
        sendNow(sendMessage);
    }

    public void sendNow(BotApiMethod<?> toSend) {
        execute(() -> telegramClient.execute(toSend));
    }

    public void sendNow(SendMessage message) {
        execute(() -> telegramClient.execute(message));
    }

    public void sendNow(String text, Long toUserId) {
        SendMessage message = AnswerMethodFactory.getSendMessage(toUserId, text, null);
        execute(() -> telegramClient.execute(message));
    }

    public void sendNow(SendVideo message) {
        execute(() -> telegramClient.execute(message));
    }

    public void sendNow(MediaMsgModel message) {
        if (!(hasPhotoMsg(message)
                || hasApiMsg(message)
                || hasMedeaGroup(message)
                || hasVideoMsg(message))) {
            return;
        }

        if (hasMedeaGroup(message)) {
            execute(() -> telegramClient.execute(message.getMediaGroup()));
            if (message.getKeyBoardMediaGroup() != null) {
                execute(() -> telegramClient.execute(message.getKeyBoardMediaGroup()));
            }
        }

        if (hasPhotoMsg(message)) {
            for (SendPhoto photo : message.getPhotoResponses()) {
                execute(() -> telegramClient.execute(photo));
            }
        }

        if (hasVideoMsg(message)) {
            for (SendVideo video : message.getVideoResponses()) {
                execute(() -> telegramClient.execute(video));
            }
        }

        if (hasApiMsg(message)) {
            for (BotApiMethod<?> answer : message.getBotApiMethods()) {
                execute(() -> telegramClient.execute(answer));
            }
        }

    }

    private boolean hasPhotoMsg(MediaMsgModel message) {
        return Optional.ofNullable(message)
                .map(MediaMsgModel::getPhotoResponses)
                .map(photos -> !photos.isEmpty())
                .orElse(false);
    }

    private boolean hasVideoMsg(MediaMsgModel message) {
        long count = Optional.ofNullable(message)
                .map(MediaMsgModel::getVideoResponses)
                .map(video -> !video.isEmpty())
                .stream().count();
        return count == 1L;
    }

    private boolean hasApiMsg(MediaMsgModel message) {
        return Optional.ofNullable(message)
                .map(MediaMsgModel::getBotApiMethods)
                .map(apis -> !apis.isEmpty())
                .orElse(false);
    }

    private boolean hasMedeaGroup(MediaMsgModel message) {
        return message.getMediaGroup() != null;
    }

    @FunctionalInterface
    private interface TelegramAction {
        void run() throws TelegramApiException;
    }

    private void execute(TelegramAction action) {
        try {
            action.run();
        } catch (TelegramApiException e) {
        }
    }


}

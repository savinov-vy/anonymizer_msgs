
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SenderService implements AdminNotifier {

    TelegramClient telegramClient;

    private static final Long CHAT_ID = -1004434014698L;
    private static final Integer TOPIC_MAFIA_ID = 5;

    private void sendSystemMessage(String chatId, String text) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
        sendNowPrivate(sendMessage);
    }

    public void sentMessage(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(chatId, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sendMafiaMessage(String text) {
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(CHAT_ID, TOPIC_MAFIA_ID, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sendMafiaMessage(String text, Integer messageId) {
        CompletableFuture.runAsync(() -> {
            try {
                CopyMessage copyMessage = CopyMessage.builder()
                        .fromChatId(CHAT_ID)
                        .messageId(messageId)
                        .chatId(CHAT_ID)
                        .messageThreadId(TOPIC_MAFIA_ID)
                        .disableNotification(true)
                        .build();
                sendNowPrivate(copyMessage);

                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(CHAT_ID)
                        .messageId(messageId)
                        .build();
                sendNowPrivate(deleteMessage);

            } catch (Exception e) {
                System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
            }
        });
        SendMessage sendMessage = AnswerMethodFactory.getSendMessage(CHAT_ID, TOPIC_MAFIA_ID, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sendMafiaMessage(Integer messageId) {
        CompletableFuture.runAsync(() -> {
            try {
                CopyMessage copyMessage = CopyMessage.builder()
                        .fromChatId(CHAT_ID)
                        .messageId(messageId)
                        .chatId(CHAT_ID)
                        .disableNotification(true)
                        .messageThreadId(TOPIC_MAFIA_ID)
                        .build();
                sendNowPrivate(copyMessage);

                DeleteMessage deleteMessage = DeleteMessage.builder()
                        .chatId(CHAT_ID)
                        .messageId(messageId)
                        .build();
                sendNowPrivate(deleteMessage);
            } catch (Exception e) {
                System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
            }
        });

    }

    public void sendHTMLMessage(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getHTMLSendMessage(chatId, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sendRedirectableMsg(String text, Long chatId) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessageRedirectable(chatId, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sentMessage(String text, String channelName) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(channelName, text, null);
        sendNowPrivate(sendMessage);
    }

    public void sentMessage(String text, Long chatId, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(chatId, text, keyboardMarkup);
        sendNowPrivate(sendMessage);
    }

    //todo переделать чтобы летело в личку бота Love38Bot - чтобы ботов для пользака не плодить
    public void sendNowPrivate(BotApiMethod<?> toSend) {
        execute(() -> telegramClient.execute(toSend));
    }

    public void sendNowPrivate(SendMessage message) {
        execute(() -> telegramClient.execute(message));
    }

    //todo переделать чтобы летело в личку бота Love38Bot - чтобы ботов для пользака не плодить
    public void sendNowPrivate(String text, Long toUserId) {
        SendMessage message = AnswerMethodFactory.getSendMessage(toUserId, text, null);
        execute(() -> telegramClient.execute(message));
    }

    public void sendNowPrivate(SendVideo message) {
        execute(() -> telegramClient.execute(message));
    }

    public void sendNowPrivate(MediaMsgModel message) {
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

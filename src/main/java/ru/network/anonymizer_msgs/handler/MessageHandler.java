package ru.network.anonymizer_msgs.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.factory.KeyboardFactory;
import ru.network.anonymizer_msgs.model.ButtonModel;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.service.SenderService;
import ru.network.anonymizer_msgs.type.ActionType;


import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

public abstract class MessageHandler {

    protected static SenderService senderService;

    @Autowired
    public void setSenderService(SenderService senderService) {
        this.senderService = senderService;
    }

    public abstract void execute(Update update, Player currentPlayer);

    public abstract ActionType getHandlerType();

    protected String getMessage(Update update) {
        return update.hasMessage() ? update.getMessage().getText() : "";
    }

    protected boolean messageTextIsEquals(Update update, String messageText) {
        return update.hasMessage() && messageText.equals(update.getMessage().getText());
    }

    protected boolean messageTextStartWith(Update update, String messageText) {
        boolean result = false;
        if (update.hasMessage() && hasText(update.getMessage().getText())) {
            String text = update.getMessage().getText();
            if (text.startsWith(messageText)) {
                result = true;
            }
        }
        return result;
    }

    protected boolean hasMessageText(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    protected void sendMessage(String text, Long chatId, List<String> btnNames, List<Integer> configs) {
        SendMessage sendMessage = getSendMessage(chatId, text, btnNames, configs);
        senderService.sendNowPrivate(sendMessage);
    }

    protected void sendMessage(String text, Long chatId, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage =
                AnswerMethodFactory.getSendMessage(chatId, text, replyKeyboard);
        senderService.sendNowPrivate(sendMessage);
    }

    protected SendMessage getSendMessage(Long chatId, String text, List<String> btnNames, List<Integer> configs) {
        return AnswerMethodFactory.getSendMessage(chatId, text, KeyboardFactory.getReplyKeyboard(btnNames, configs));
    }

    public void sendUserMafiaMessage(Message original) {
        senderService.sendUserMafiaMessage(original);
    }

    protected void sendMessage(String text, Long chatId) {
        senderService.sentMessage(text, chatId);
    }

    protected void deleteMessage(DeleteMessage deleteMessage) {
        senderService.deleteMessage(deleteMessage);
    }

    protected void deleteMessage(Update update) {
        DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(update);
        if (deleteMessage == null) {
            return;
        }
        senderService.deleteMessage(deleteMessage);
    }

    protected void sendHTMLMessage(String text, Long chatId) {
        senderService.sendHTMLMessage(text, chatId);
    }

    protected boolean hasCommand(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            return hasText(message.getText()) && message.getText().charAt(0) == '/';
        }
        return false;
    }


    protected void sendCallbackMessage(String text, Long toUserId, List<String> btnNames, List<ButtonModel> buttons, List<Integer> config) {
        BotApiMethod<?> toSend = AnswerMethodFactory.getSendMessage(toUserId, text,
                KeyboardFactory.getInlineKeyboard(btnNames, config, buttons));
        senderService.sendNowPrivate(toSend);
    }

}

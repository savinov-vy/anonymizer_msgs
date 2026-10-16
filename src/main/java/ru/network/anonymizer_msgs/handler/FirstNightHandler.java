package ru.network.anonymizer_msgs.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FirstNightHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        if (update.hasMessage()) {
            int messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(chatId, messageId);
            deleteMessage(deleteMessage);
        }
    }

    public ActionType getHandlerType(){
        return ActionType.FIRST_NIGHT;
    }

}

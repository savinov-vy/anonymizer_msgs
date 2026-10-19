package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;


public class RegistrationHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        if (update.hasMessage()) {
            sendUserMafiaMessage(update.getMessage());
        }
    }

    public ActionType getHandlerType(){
        return ActionType.REGISTERED;
    }

}

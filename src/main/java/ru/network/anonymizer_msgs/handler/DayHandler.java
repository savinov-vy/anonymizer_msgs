package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;


public class DayHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {

    }

    public void activateHandler(Player player) {

    }

    public ActionType getHandlerType(){
        return ActionType.DAY;
    }

}

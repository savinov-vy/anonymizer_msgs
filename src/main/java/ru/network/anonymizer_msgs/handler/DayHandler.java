package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DayHandler extends MessageHandler {

    public static final Map<Long, Long> voterIdTargetId = new ConcurrentHashMap<>();

    public void execute(Update update, Player currentPlayer) {

    }

    public ActionType getHandlerType(){
        return ActionType.DAY;
    }

}

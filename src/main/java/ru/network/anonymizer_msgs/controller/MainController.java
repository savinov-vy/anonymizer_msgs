package ru.network.anonymizer_msgs.controller;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.handler.MessageHandler;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.utils.MessageHandlerUtils;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MainController {

    Map<ActionType, MessageHandler> messageHandlerMap;

    public MainController(List<MessageHandler> actions) {
        this.messageHandlerMap = MessageHandlerUtils.getHandlerByTypeMap(actions);
    }

    public void distribute(Update update, Player currentPlayer) {

        MessageHandler messageHandler = messageHandlerMap.get(currentPlayer.getActionType());
        if (messageHandler == null) {
            messageHandler = messageHandlerMap.get(currentPlayer.getActionType());
        }
        messageHandler.execute(update, currentPlayer);
    }

}

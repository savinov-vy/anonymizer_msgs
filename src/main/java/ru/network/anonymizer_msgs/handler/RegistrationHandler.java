package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;


public class RegistrationHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        // в целом тут можно отправлять все сообщения в чат под рандомными именами
        // только нужно проверять что песонаж зарегистрирован Love38Bot
    }

    public ActionType getHandlerType(){
        return ActionType.REGISTERED;
    }

}

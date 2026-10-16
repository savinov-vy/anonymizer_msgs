package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;


public class RegistrationHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        // если игрок сюда пришел значит они либо уже был зарегистрирован либо его сейчас зарегистрировали; посмотреть по времени регистрации

        //             senderService.sendMafiaMessage(String.format("Игрок %s присоединился! Всего игроков %s/10", newPlayerName, getAll().size()));
//        if (isPlayersFull()) {
//            sendMessage("Извините, игра уже заполнена (10/10).", currentPlayer.getId());
//        }
//
//        // Временно создаем игрока без роли (роль назначим при старте)
//        players.put(id, new Player(id, name, null));
//
//        notifyAllAsync("Игрок " + name + " присоединился! Всего игроков: " + players.size() + "/10");
//
//        if (isPlayersFull()) {
//            startFirstNight();
//            return true;
//        }
//        return false;
    }

    public ActionType getHandlerType(){
        return ActionType.REGISTERED;
    }

}

package ru.network.anonymizer_msgs.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;


public class NightHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        // ночью еще шериф может искать босса
        // а босс может искать шерифа
        // это обработка колбеков чтобы не плодить ботов для пользователелей можно отправлять прямо в
        // Love38 и там сразу пересылать пользователю ответы


        // Логика отправки секретных сообщений в чат мафии
//        if (message.getChat().isUserChat()) {
//            String text = message.getText();
//            try {
//                gameService.sendMafiaChatMessage(chatId, text);
//                broadcastToMafia(chatId, message.getMessageId());
//                return null;
//            } catch (IllegalStateException e) {
//                sendSystemMessage(chatId, "⚠️ Ошибка: " + e.getMessage());
//                return null;
//            } catch (SecurityException e) {
//                // Пишет обычный мирный житель — пускаем управление дальше в ваш анонимайзер
//            }
//        }
    }

    public void activateHandler(Player player) {

    }

    public ActionType getHandlerType(){
        return ActionType.NIGHT;
    }

}

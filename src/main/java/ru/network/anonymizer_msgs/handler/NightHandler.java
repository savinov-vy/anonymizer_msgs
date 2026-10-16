package ru.network.anonymizer_msgs.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.factory.AnswerMethodFactory;
import ru.network.anonymizer_msgs.model.CallBackDataPlayerIdDto;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.service.CallBackQueryService;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.Role;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NightHandler extends MessageHandler {

    CallBackQueryService callBackQueryService;
    public static final Map<Long, Long> shooterIdTargetId = new ConcurrentHashMap<>();

    public void execute(Update update, Player currentPlayer) {
        if (update.hasCallbackQuery()) {
            CallBackDataPlayerIdDto callBackData = callBackQueryService.getCallBackData(update);
            String data = update.getCallbackQuery().getData();
            if (currentPlayer.isAlive() && (currentPlayer.getRole() == Role.DON
                    || currentPlayer.getRole() == Role.MAFIA)) {
                shooterIdTargetId.put(currentPlayer.getId(), callBackData.getUserId());
            }

        } else if (update.hasMessage()) {
            int messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(chatId, messageId);
            deleteMessage(deleteMessage);
        }
    }

    public void activateHandler(Player player) {

    }

    public ActionType getHandlerType(){
        return ActionType.NIGHT;
    }

}

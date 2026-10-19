package ru.network.anonymizer_msgs.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.type.ActionType;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FirstDayHandler extends MessageHandler {

    public void execute(Update update, Player currentPlayer) {
        // нужно добавить что пользователь в игре, иначе просто удаляем
        if (update.hasMessage()) {
            sendUserMafiaMessage(update.getMessage());
        }
    }

    public ActionType getHandlerType(){
        return ActionType.FIRST_DAY;
    }

}

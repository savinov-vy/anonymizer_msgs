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
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.service.CallBackQueryService;
import ru.network.anonymizer_msgs.service.GamePhaserService;
import ru.network.anonymizer_msgs.service.SenderService;
import ru.network.anonymizer_msgs.type.ActionType;
import ru.network.anonymizer_msgs.type.Role;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NightHandler extends MessageHandler {

    CallBackQueryService callBackQueryService;
    public static final Map<Long, Long> shooterIdTargetId = new ConcurrentHashMap<>();
    PlayerRepository playerRepository;
    GamePhaserService gamePhaserService;
    SenderService senderService;
    private static final List<String> FAST_CHOOSE_MSG = List.of("\uD83E\uDE78 Мафия сделала выбор. \uD83C\uDF11 " +
            "Город об этом ещё не знает — но к утру узнает. Всегда узнаёт. ⚰\uFE0F",
            "\uD83D\uDD2A Мафия сделала выбор. \uD83D\uDC80 Где-то в этом городе человек ещё спит, не подозревая, что его уже нет. " +
                    "\uD83D\uDE36\u200D\uD83C\uDF2B\uFE0F Просто тело об этом узнает позже. \uD83D\uDD6F\uFE0F",
            "\uD83C\uDFAD Мафия сделала выбор. \uD83E\uDE78 И самое страшное — не то, кого выбрали. \uD83D\uDE28 Самое страшное — как легко это далось." +
                    " \uD83E\uDD76 Никто не дрогнул. " +
                    "Никто не отвёл глаз. ☕ Мы просто продолжили пить чай. \uD83D\uDC80",
            "⚰\uFE0F Мафия сделала выбор. \uD83D\uDC80 Смерть уже надевает пальто и выходит за дверь." +
                    " \uD83D\uDEAA\uD83D\uDDA4 Она всегда одевается не спеша — знает, что успеет. ⏳",
            "\uD83D\uDD2A Мафия сделала выбор. \uD83E\uDE78 Остальное — дело времени и случая." +
                    " ⏳ А случай, как известно, любит таких, как мы. \uD83D\uDE08\uD83C\uDF11");

    public void execute(Update update, Player currentPlayer) {
        if (update.hasCallbackQuery()) {
            CallBackDataPlayerIdDto callBackData = callBackQueryService.getCallBackData(update);
            if (currentPlayer.isAlive() && (currentPlayer.getRole() == Role.DON
                    || currentPlayer.getRole() == Role.MAFIA)) {
                shooterIdTargetId.put(currentPlayer.getId(), callBackData.getUserId());
            }
            if (amountShooting().equals(playerRepository.findAliveBlackPlayers().size())) {
                senderService.sendMafiaMessage(getRandom(FAST_CHOOSE_MSG));
                gamePhaserService.setTimer(3);
            }
        } else if (update.hasMessage()) {
            int messageId = update.getMessage().getMessageId();
            long chatId = update.getMessage().getChatId();
            DeleteMessage deleteMessage = AnswerMethodFactory.getDeleteMessage(chatId, messageId);
            deleteMessage(deleteMessage);
        }
    }

    private static String getRandom(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(descriptions.size());
        return descriptions.get(index);
    }

    public Integer amountShooting() {
        return shooterIdTargetId.size();
    }

    public ActionType getHandlerType(){
        return ActionType.NIGHT;
    }

}

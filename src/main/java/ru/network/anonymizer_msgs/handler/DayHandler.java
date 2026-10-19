package ru.network.anonymizer_msgs.handler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.network.anonymizer_msgs.model.CallBackDataPlayerIdDto;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.repository.PlayerRepository;
import ru.network.anonymizer_msgs.service.CallBackQueryService;
import ru.network.anonymizer_msgs.service.GamePhaserService;
import ru.network.anonymizer_msgs.type.ActionType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DayHandler extends MessageHandler {

    PlayerRepository playerRepository;
    GamePhaserService gamePhaserService;
    CallBackQueryService callBackQueryService;
    public static final Map<Long, Long> voterIdTargetId = new ConcurrentHashMap<>();
    public static final AtomicBoolean isSendMsg = new AtomicBoolean(false);

    private static final List<String> NEW_TARGET_MSGS = List.of(
            "⚔\uFE0F %s → %s. \uD83D\uDCCA %s",
            "\uD83D\uDD25 %s → %s. \uD83D\uDCCA %s",
            "\uD83D\uDCA5 %s → %s. \uD83D\uDC4E %s",
            "\uD83E\uDE93 %s → %s. \uD83D\uDC4E %s",
            "⚖\uFE0F %s → %s. \uD83D\uDC4E %s",
            "\uD83D\uDD25 %s → %s. \uD83D\uDD3B %s",
            "\uD83C\uDFAF %s → %s. \uD83D\uDDE1\uFE0F %s",
            "\uD83D\uDDE1\uFE0F %s → %s. \uD83D\uDD2A %s",
            "\uD83D\uDDE1\uFE0F %s → %s. \uD83D\uDE80 %s",
            "\uD83E\uDE93 %s → %s. \uD83D\uDCA3 %s",
            "⚡ %s → %s. \uD83D\uDD2A %s",
            "⚡ %s → %s. \uD83D\uDCA3 %s",
            "\uD83D\uDD25 %s → %s. \uD83D\uDC4E %s",
            "⚡ %s → %s. \uD83E\uDE93 %s",
            "\uD83D\uDDE1\uFE0F %s → %s. \uD83D\uDCA3 %s"
    );

    private static final List<String> FAST_CHOOSE_MSG = List.of(
            "⚰\uFE0F Город сделал выбор. ⏳ Но у тебя есть 2 минуты — можно переголосовать. \uD83D\uDC80 Шанс ещё есть.",
            "\uD83E\uDE78 Выбор сделан. ⏳ 2 минуты — это твой шанс. \uD83D\uDC80 Используй его.",
            "\uD83D\uDDF3\uFE0F Город проголосовал. ⏳ Ещё 2 минуты — можно переголосовать. \uD83D\uDC80 Успей. Шанс есть.",
            "⚰\uFE0F Выбор сделан. ⏳ 2 минуты — твой последний шанс. \uD83D\uDC80 Не упусти.",
            "\uD83E\uDE78 Город сделал выбор. ⏳ 2 минуты — можно переголосовать. \uD83D\uDC80 Кто-то ещё может спастись.",
            "\uD83E\uDEA6 Город сделал выбор. ⏳ Но у тебя есть 2 минуты — можно переголосовать. \uD83D\uDD78\uFE0F Шанс ещё есть.",
            "\uD83E\uDE78 Выбор сделан. ⏳ 2 минуты — это твой шанс. \uD83E\uDEC0 Используй его.",
            "\uD83E\uDE78 Город сделал выбор. ⏳ 2 минуты — можно переголосовать. \uD83D\uDD77\uFE0F Кто-то ещё может спастись.\n",
            "\uD83D\uDE31 Выбор сделан. \uD83E\uDE78 Город не колебался. ⏳ 2 минуты — можно переголосовать. \uD83D\uDD77\uFE0F Кто-то ещё может спастись.",
            "\uD83D\uDE31 Выбор сделан. \uD83D\uDC80 Город не колебался. ⏳ 2 минуты — можно переголосовать. \uD83E\uDE78 Кто-то ещё может спастись.",
            "\uD83D\uDE31 Выбор сделан. \uD83D\uDC80 Город решил. ⏳ 2 минуты — твой шанс. ⚰\uFE0F Используй.",
            "\uD83D\uDC80 Выбор сделан. \uD83D\uDE31 Город сказал своё слово. ⏳ Ещё 2 минуты — можно переголосовать. ⚰\uFE0F Успей. Шанс есть.",
            "\uD83D\uDC80 Выбор сделан. \uD83D\uDD78\uFE0F Город решил. ⏳ Но у тебя есть 2 минуты — можно переголосовать. \uD83E\uDE78 Шанс ещё есть.",
            "\uD83D\uDC80 Выбор сделан. \uD83D\uDD78\uFE0F Город не колебался. ⏳ 2 минуты — можно переголосовать. \uD83E\uDE78 Кто-то ещё может спастись.");


    public void execute(Update update, Player currentPlayer) {
        if (update.hasMessage() && currentPlayer.isAlive()) {
            sendUserMafiaMessage(update.getMessage());
        } else if (update.hasCallbackQuery() && currentPlayer.isAlive()) {
            CallBackDataPlayerIdDto callBackData = callBackQueryService.getCallBackData(update);
            if (currentPlayer.isAlive()) {
                voterIdTargetId.put(currentPlayer.getId(), callBackData.getUserId());
                if (isSendMsg.get()) {
                    String voterName = playerRepository.findById(currentPlayer.getId()).getName();
                    String targetName = playerRepository.findById(callBackData.getUserId()).getName();
                    getAmountVote()
                    senderService.sendMafiaMessage(
                            playerRepository.findById(String.format(getRandom(NEW_TARGET_MSGS), playerRepository.findById()currentPlayer.getId().getName()
                    );
                }
            }
            if (amountVoters().equals(playerRepository.findAliveBlackPlayers().size())) {
                if (!isSendMsg.get()) {
                    senderService.sendMafiaMessage(getRandom(FAST_CHOOSE_MSG));
                    gamePhaserService.setTimer(120);
                }

            }
        } else
    }

    public static void resetCashes() {
        voterIdTargetId.clear();
        isSendMsg.set(false);
    }

    private static String getRandom(List<String> descriptions) {
        if (descriptions == null || descriptions.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(descriptions.size());
        return descriptions.get(index);
    }

    public Integer amountVoters() {
        return voterIdTargetId.size();
    }

    public ActionType getHandlerType(){
        return ActionType.DAY;
    }

}

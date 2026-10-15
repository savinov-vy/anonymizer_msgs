package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.service.MafiaGameService;
import ru.network.anonymizer_msgs.service.SenderService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class WebHookBot {

    private final TelegramClient telegramClient;
    private final MediaGroupBuffer mediaGroupBuffer;
    private final MafiaGameService gameService;
    private final SenderService senderService;

    public WebHookBot(TelegramClient telegramClient, MafiaGameService gameService,
                      SenderService senderService) {
        this.telegramClient = telegramClient;
        this.gameService = gameService;
        this.mediaGroupBuffer = new MediaGroupBuffer(telegramClient);
        this.senderService = senderService;

        // Настраиваем мост уведомлений: когда сервис меняет фазу или раздает роли,
        // бот автоматически рассылает сообщения нужным игрокам
        this.gameService.setNotificationListener(event -> {
            String[] parts = event.split(":", 2);
            String target = parts[0];
            String text = parts[1];

            if (target.equals("ALL")) {
                // Рассылка вообще всем участникам лобби
                senderService.sendMafiaMessage(text);
            } else {
                // Персональное сообщение конкретному игроку (например, его роль)
                sendSystemMessage(target, text);
            }
        });
    }

    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();

            if (message.getFrom() != null && message.getFrom().getIsBot()) {
                return null;
            }

            String chatId = message.getChatId().toString();

            // обработка регистрации
            if (message.hasText() && !gameService.isPlayersFull()) {
                try {
                    String userid = extractUserId(update);
                    String firstName = message.getFrom().getFirstName();
                    gameService.registerPlayer(userid, firstName);
                } catch (IllegalStateException e) {
                    senderService.sendMafiaMessage("⚠️ " + e.getMessage());
                }
                return null;
            }

            // Логика отправки секретных сообщений в чат мафии
            if (message.getChat().isUserChat()) {
                String text = message.getText();
                try {
                    gameService.sendMafiaChatMessage(chatId, text);
                    broadcastToMafia(chatId, message.getMessageId());
                    return null;
                } catch (IllegalStateException e) {
                    sendSystemMessage(chatId, "⚠️ Ошибка: " + e.getMessage());
                    return null;
                } catch (SecurityException e) {
                    // Пишет обычный мирный житель — пускаем управление дальше в ваш анонимайзер
                }
            }

            if (message.getMediaGroupId() != null) {
                mediaGroupBuffer.addMessage(message);
                return null;
            }

            // Стандартное одиночное зеркалирование анонимайзера для мирных жителей
            Integer messageId = message.getMessageId();
            senderService.sendMafiaMessage(messageId);

        }
        return null;
    }

    private void broadcastToMafia(String senderChatId, Integer messageId) {
        CompletableFuture.runAsync(() -> {
            try {
                Player sender = gameService.getPlayer(senderChatId);
                List<Player> allPlayers = gameService.getAllPlayers();

                for (Player recipient : allPlayers) {
                    if (recipient.getRole() != null && recipient.getRole().isBlack()
                            && recipient.isAlive() && !recipient.getId().equals(senderChatId)) {

                        CopyMessage copyMessage = CopyMessage.builder()
                                .fromChatId(senderChatId)
                                .messageId(messageId)
                                .chatId(recipient.getId())
                                .build();
                        telegramClient.execute(copyMessage);
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка рассылки чата мафии: " + e.getMessage());
            }
        });
    }

    private void sendSystemMessage(String chatId, String text) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build();
            telegramClient.execute(sendMessage);
        } catch (Exception e) {
            System.err.println("Ошибка отправки системного сообщения: " + e.getMessage());
        }
    }

    private String extractUserId(Update update) {
        org.telegram.telegrambots.meta.api.objects.User from = null;

        if (update.hasMessage()) {
            from = update.getMessage().getFrom();
        } else if (update.hasCallbackQuery()) {
            from = update.getCallbackQuery().getFrom();
        } else if (update.getMyChatMember() != null) {
            from = update.getMyChatMember().getFrom();
        }

        if (from != null) {
            return String.valueOf(from.getId());
        }

        if (update.hasMessage()) {
            return String.valueOf(update.getMessage().getChatId());
        }
        return null;
    }
}

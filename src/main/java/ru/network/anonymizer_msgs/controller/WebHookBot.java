package ru.network.anonymizer_msgs.controller;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.network.anonymizer_msgs.model.Player;
import ru.network.anonymizer_msgs.service.MafiaGameService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class WebHookBot {

    private final TelegramClient telegramClient;
    private final MediaGroupBuffer mediaGroupBuffer;
    private final MafiaGameService gameService;

    public WebHookBot(TelegramClient telegramClient, MafiaGameService gameService) {
        this.telegramClient = telegramClient;
        this.gameService = gameService;
        this.mediaGroupBuffer = new MediaGroupBuffer(telegramClient);

        // Настраиваем мост уведомлений: когда сервис меняет фазу или раздает роли,
        // бот автоматически рассылает сообщения нужным игрокам
        this.gameService.setNotificationListener(event -> {
            String[] parts = event.split(":", 2);
            String target = parts[0];
            String text = parts[1];

            if (target.equals("ALL")) {
                // Рассылка вообще всем участникам лобби
                for (Player p : gameService.getAllPlayers()) {
                    sendSystemMessage(p.getId(), text);
                }
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

            if (message.getChat().isUserChat()) {
                String text = message.getText();

                // Перехватываем команду /start
                if (text != null && text.startsWith("/start")) {
                    try {
                        String firstName = message.getFrom().getFirstName();
                        gameService.registerPlayer(chatId, firstName);
                    } catch (IllegalStateException e) {
                        sendSystemMessage(chatId, "⚠️ " + e.getMessage());
                    }
                    return null;
                }

                // Логика отправки секретных сообщений в чат мафии
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
            Integer threadId = message.getMessageThreadId();

            CompletableFuture.runAsync(() -> {
                try {
                    CopyMessage copyMessage = CopyMessage.builder()
                            .fromChatId(chatId)
                            .messageId(messageId)
                            .chatId(chatId)
                            .messageThreadId(threadId)
                            .build();
                    telegramClient.execute(copyMessage);

                    DeleteMessage deleteMessage = DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build();
                    telegramClient.execute(deleteMessage);
                } catch (Exception e) {
                    System.err.println("Ошибка одиночного зеркалирования: " + e.getMessage());
                }
            });
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
}

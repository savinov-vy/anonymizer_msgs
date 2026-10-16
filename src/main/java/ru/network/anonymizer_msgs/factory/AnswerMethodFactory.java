package ru.network.anonymizer_msgs.factory;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

public class AnswerMethodFactory {

    /**
     * SendMessage - метод для отправки сообщения ботом пользователю
     */
    public static SendMessage getSendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(true)
                .build();
    }

    /**
     * SendMessage - метод для отправки сообщения в топик чата
     */
    public static SendMessage getSendMessage(Long chatId, Integer topicId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageThreadId(topicId)
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableNotification(true)
                .disableWebPagePreview(true)
                .protectContent(true)
                .build();
    }

    /**
     * EditMessageText - метод для редактирования текста сообщения.
     * messageThreadId не требуется — достаточно chatId и messageId.
     */
    public static EditMessageText getEditMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboard) {
        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .build();
    }

    /**
     * SendMessage - метод для отправки сообщения ботом пользователю
     */
    public static SendMessage getHTMLSendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(true)
                .parseMode(ParseMode.HTML)
                .build();
    }

    public static SendMessage getSendMessageRedirectable(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(false)
                .build();
    }

    /**
     * SendMessage - id - текстом
     * например для отправки в телеграмм канал
     */
    public static SendMessage getSendMessage(String chatId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(true)
                .build();
    }

    public static SendMessage getSendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(true)
                .build();
    }

    public static SendMessage getSendMessageNotProtected(Long chatId, String text, ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(false)
                .build();
    }

    /**
     * удалить клавиатуру
     */
    public static SendMessage removeKeyboard(Long chatId, String text) {

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .replyMarkup(ReplyKeyboardRemove.builder()
                        .removeKeyboard(true)
                        .build()) // передаем объект удаления
                .disableWebPagePreview(true)
                .build();
    }

    /**
     * SendPhoto - метод для отправки фото сообщения ботом пользователю
     */
    public static SendPhoto getPhotoMessage(Long chatId, String text, String fileId, ReplyKeyboard keyboard) {
        return SendPhoto.builder()
                .chatId(chatId)
                .caption(text)
                .photo(new InputFile(fileId))
                .replyMarkup(keyboard)
                .protectContent(true)
                .build();
    }

    /**
     * SendVideo - метод для отправки видео сообщения ботом пользователю
     */
    public static SendVideo getVideoMessage(Long chatId, String text, String fileId, ReplyKeyboard keyboard) {
        return SendVideo.builder()
                .chatId(chatId)
                .caption(text)
                .video(new InputFile(fileId))
                .replyMarkup(keyboard)
                .protectContent(true)
                .supportsStreaming(true)
                .parseMode("HTML")
                .build();
    }

    /**
     * SendVideo - метод для отправки видео сообщения ботом пользователю
     */
    public static SendVideo getVideoMessage(Long chatId, String text, String fileId, ReplyKeyboard keyboard, Boolean isProtectionContent) {
        return SendVideo.builder()
                .chatId(chatId)
                .caption(text)
                .video(new InputFile(fileId))
                .replyMarkup(keyboard)
                .protectContent(isProtectionContent)
                .supportsStreaming(true)
                .parseMode("HTML")
                .build();
    }

    /**
     * EditMessageText - метод для редактирования текста и клавиатуры сообщения
     */
    public static EditMessageText getEditMessageText(CallbackQuery callbackQuery,
                                              String newText,
                                              InlineKeyboardMarkup keyboard) {
        return EditMessageText.builder()
                .chatId(String.valueOf(callbackQuery.getMessage().getChatId()))
                .messageId(callbackQuery.getMessage().getMessageId())
                .text(hasText(newText) ? newText : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .build();
    }

    public static SendMediaGroup getMediaGroup(Long chatId, String caption,
                                        List<String> photoIds,
                                        List<String> videoIds) {

        List<InputMedia> media = new ArrayList<>();

        // Добавляем фото
        if (photoIds != null && !photoIds.isEmpty()) {
            for (String photoId : photoIds) {
                InputMediaPhoto photo = InputMediaPhoto.builder()
                        .media(photoId)
                        .build();
                media.add(photo);
            }
        }

        // Добавляем видео
        if (videoIds != null && !videoIds.isEmpty()) {
            for (String videoId : videoIds) {
                InputMediaVideo video = InputMediaVideo.builder()
                        .media(videoId)
                        .build();
                media.add(video);
            }
        }

        // Проверяем, что есть медиа и добавляем подпись к первому элементу
        if (!media.isEmpty() && hasText(caption)) {
            media.get(0).setCaption(caption);
            // Устанавливаем parse_mode если нужно
            media.get(0).setParseMode("HTML"); // или "Markdown"
        }

        return SendMediaGroup.builder()
                .chatId(chatId)
                .protectContent(true)
                .medias(media)
                .build();
    }

    /**
     * DeleteMessage - метод для удаления конкретного сообщения
     */
    public static DeleteMessage getDeleteMessage(Long chatId, Integer messageId) {
        return DeleteMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .build();
    }

    /**
     * DeleteMessage - метод для удаления конкретного сообщения
     */
    public static DeleteMessage getDeleteMessage(Update update) {
        if (!update.hasMessage()) {
            return null;
        }
        Integer messageId = update.getMessage().getMessageId();
        Long chatId = update.getMessage().getChatId();

        if (messageId == null || chatId == chatId) {
            return null;
        }
        return DeleteMessage.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .build();
    }

    public static AnswerCallbackQuery getAnswerCallbackQuery(String callbackQueryId, String text) {
        return AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .build();
    }

}

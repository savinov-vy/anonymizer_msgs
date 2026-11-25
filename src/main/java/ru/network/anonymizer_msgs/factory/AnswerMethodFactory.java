package ru.network.anonymizer_msgs.factory;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.ReplyParameters;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

public class AnswerMethodFactory {



    private static final int MAX_QUOTE_LENGTH = 300;

    // ================== ЦИТАТА ==================

    /**
     * Экранирует HTML-спецсимволы, чтобы не сломать parseMode=HTML.
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * Строит цитату предыдущего сообщения в виде HTML blockquote:
     * <blockquote>[Имя]: текст</blockquote>
     *
     * @param senderName уже отформатированное имя (может содержать HTML, напр. [<b>Name</b>])
     * @param text       исходный текст (будет экранирован)
     */
    public static String buildQuote(String senderName, String text) {
        if (!hasText(text)) {
            return "";
        }
        String safeText = escapeHtml(text);
        if (safeText.length() > MAX_QUOTE_LENGTH) {
            safeText = safeText.substring(0, MAX_QUOTE_LENGTH) + "…";
        }
        String name = hasText(senderName) ? senderName : "[<b>Неизвестный</b>]";
        return "<blockquote>" + name + ": " + safeText + "</blockquote>\n";
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
                .protectContent(false)
                .parseMode(ParseMode.HTML)
                .build();
    }

    // Текстовый — БЕЗ replyParameters
    public static SendMessage getSendMessageRedirectable(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .protectContent(false)
                .build();
    }

    // Для фото
    public static SendPhoto getSendPhotoRedirectable(Long chatId, Integer replyToMessageId, String fileId, String caption, ReplyKeyboardMarkup keyboard) {
        return SendPhoto.builder()
                .chatId(String.valueOf(chatId))
                .photo(new InputFile(fileId))
                .caption(caption)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .replyParameters(replyToMessageId != null ? ReplyParameters.builder().messageId(replyToMessageId).build() : null)
                .protectContent(false)
                .build();
    }

    // Для видео
    public static SendVideo getSendVideoRedirectable(Long chatId, Integer replyToMessageId, String fileId, String caption, ReplyKeyboardMarkup keyboard) {
        return SendVideo.builder()
                .chatId(String.valueOf(chatId))
                .video(new InputFile(fileId))
                .caption(caption)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .replyParameters(replyToMessageId != null ? ReplyParameters.builder().messageId(replyToMessageId).build() : null)
                .protectContent(false)
                .build();
    }

    // Для голосовых
    public static SendVoice getSendVoiceRedirectable(Long chatId, Integer replyToMessageId, String fileId, String caption, ReplyKeyboardMarkup keyboard) {
        return SendVoice.builder()
                .chatId(String.valueOf(chatId))
                .voice(new InputFile(fileId))
                .caption(caption)
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .replyParameters(replyToMessageId != null ? ReplyParameters.builder().messageId(replyToMessageId).build() : null)
                .protectContent(false)
                .build();
    }

    // Ваш старый метод для текста оставляем


    public static SendMessage getSendMessageRedirectable(
            Long chatId,
            Integer replyToMessageId, // Добавили ID сообщения, на которое отвечаем
            String text,
            ReplyKeyboardMarkup keyboard) {

        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .parseMode(ParseMode.HTML)
                .replyParameters(replyToMessageId != null
                        ? ReplyParameters.builder().messageId(replyToMessageId).build()
                        : null)
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
                .protectContent(false)
                .build();
    }

    public static SendMessage getSendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        return SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(hasText(text) ? text : "\uD83D\uDE0A \uD83D\uDC4D")
                .replyMarkup(keyboard)
                .disableWebPagePreview(true)
                .protectContent(false)
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
                .protectContent(false)
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

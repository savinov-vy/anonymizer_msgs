package ru.network.anonymizer_msgs.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.network.anonymizer_msgs.model.ButtonModel;
import ru.network.anonymizer_msgs.utils.ObjectMapperUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.network.anonymizer_msgs.constant.ButtonConstant.BTN_EXCEPTION;

public class KeyboardFactory {

	/**
	 * рекомендует пользователю какое текстовое сообщение отправить
	 */
	public static ReplyKeyboardMarkup getReplyKeyboard(List<String> text,
												List<Integer> configuration) {
		List<KeyboardRow> keyboard = new ArrayList<>();
		int index = 0;

		for (Integer rowNumber : configuration) {
			KeyboardRow row = new KeyboardRow();
			for (int i = 0; i < rowNumber; i++) {
				KeyboardButton button = KeyboardButton.builder()
						.text(text.get(index))
								.build();
				row.add(button);
				index += 1;
			}
			keyboard.add(row);
		}
		return ReplyKeyboardMarkup.builder()
				.resizeKeyboard(true)
				.keyboard(keyboard)
						.build();
	}

	public static ReplyKeyboardRemove removeKeyboard() {
		return ReplyKeyboardRemove.builder()
				.removeKeyboard(true)
				.build();
	}

	public static InlineKeyboardMarkup getInlineKeyboard(List<String> text,
												  List<Integer> configuration,
												  List<ButtonModel> buttons) {
		List<InlineKeyboardRow> keyboard = new ArrayList<>();
		int index = 0;

		for (Integer rowNumber : configuration) {
			InlineKeyboardRow row = new InlineKeyboardRow();
			for (int i = 0; i < rowNumber; i++) {
				if (text.size() < index - 1) {
					throw new RuntimeException(BTN_EXCEPTION);
				}
				InlineKeyboardButton button = InlineKeyboardButton.builder()
						.text(text.get(index))
						.callbackData(ObjectMapperUtils.writeString(buttons.get(index).getCallBackDataAnonDto()))
						.build();
				row.add(button);
				index += 1;
			}
			keyboard.add(row);
		}
		return InlineKeyboardMarkup.builder()
				.keyboard(keyboard)
				.build();
	}
}

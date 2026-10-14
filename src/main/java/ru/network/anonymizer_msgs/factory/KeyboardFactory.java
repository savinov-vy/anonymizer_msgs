package ru.network.anonymizer_msgs.factory;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.network.anonymizer_msgs.utils.ObjectMapperUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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

}

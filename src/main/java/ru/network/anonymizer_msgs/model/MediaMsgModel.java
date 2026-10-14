package ru.network.anonymizer_msgs.model;

import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;

import java.util.List;

@Data
@Builder
public class MediaMsgModel {
	List<BotApiMethod<?>> botApiMethods;
	List<SendPhoto> photoResponses;
	List<SendVideo> videoResponses;
	SendMediaGroup mediaGroup;
	BotApiMethod<?> keyBoardMediaGroup;
	Long toChatId;
}

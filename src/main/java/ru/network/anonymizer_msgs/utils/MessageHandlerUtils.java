package ru.network.anonymizer_msgs.utils;

import ru.network.anonymizer_msgs.handler.MessageHandler;
import ru.network.anonymizer_msgs.type.ActionType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageHandlerUtils {
	public static Map<ActionType, MessageHandler> getHandlerByTypeMap(List<MessageHandler> handlerMessageHandlers) {
		return handlerMessageHandlers.stream()
				.collect(Collectors.toMap(
						MessageHandler::getHandlerType,
						handlerMessageHandler -> handlerMessageHandler
				));
	}
}

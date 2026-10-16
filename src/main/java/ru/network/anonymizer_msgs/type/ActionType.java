package ru.network.anonymizer_msgs.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ActionType {
	REGISTERED,
	FIRST_NIGHT,
	FIRST_DAY,
	DAY,
	NIGHT,
	FINISHED
}

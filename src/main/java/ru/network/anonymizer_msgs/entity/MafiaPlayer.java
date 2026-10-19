package ru.network.anonymizer_msgs.entity;

import ru.network.anonymizer_msgs.type.MafiaPlayerColor;

/**
 * сохранение уже после регистрации
 * до окончания регистрации смысла сохранять нет
 */
public abstract class MafiaPlayer {
	public abstract Long getId();
	public abstract MafiaPlayerColor getColor();
	public abstract boolean isLead();
	public abstract String getName();
}

package ru.network.anonymizer_msgs.entity;

import ru.link.network.type.MafiaColor;

/**
 * сохранение уже после регистрации
 * до окончания регистрации смысла сохранять нет
 */
public abstract class MafiaPlayer {
	public abstract Long getId();
	public abstract MafiaColor getColor();
	public abstract boolean isLead();
	public abstract String getName();
}

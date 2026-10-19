package ru.network.anonymizer_msgs.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.link.network.type.MafiaColor;

/**
 * сохранение уже после регистрации
 * до окончания регистрации смысла сохранять нет
 */
@Getter
@Setter
@Entity
@Builder
@Table(name = "mafia_player_black")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MafiaPlayerBlack extends MafiaPlayer {

	/**
	 * совпадает с userId и chatId
	 */
	@Id
	@Column(name = "player_id")
	Long playerId;

	/**
	 * игровое имя
	 */
	@Column(name = "player_name")
	String playerName;

	/**
	 * является ли главой мафии
	 */
	@Column(name = "is_boss")
	Boolean isBoss;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mafia_game_id")
	MafiaGame mafiaGame;

	@Override
	public Long getId() {
		return playerId;
	}
	@Override
	public MafiaColor getColor() {
		return MafiaColor.BLACK;
	}
	@Override
	public boolean isLead() {
		return isBoss;
	}
	@Override
	public String getName() {
		return playerName;
	}

}

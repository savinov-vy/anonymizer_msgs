package ru.network.anonymizer_msgs.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.network.anonymizer_msgs.type.MafiaPlayerColor;

/**
 * сохранение уже после регистрации
 * до окончания регистрации смысла сохранять нет
 */
@Getter
@Setter
@Entity
@Builder
@Table(name = "mafia_player_red")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MafiaPlayerRed extends MafiaPlayer {

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
	 * является ли шерифом
	 */
	@Column(name = "is_sheriff")
	Boolean isSheriff;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mafia_game_id")
	MafiaGame mafiaGame;

	@Override
	public Long getId() {
		return playerId;
	}

	@Override
	public MafiaPlayerColor getColor() {
		return MafiaPlayerColor.RED;
	}

	@Override
	public boolean isLead() {
		return isSheriff;
	}

	@Override
	public String getName() {
		return playerName;
	}
}

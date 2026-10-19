
package ru.network.anonymizer_msgs.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * то что пользователь сам создал о себе
 */
@Data
@Entity
@Builder
@Table(name = "mafia_game")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MafiaGame {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	/**
	 * комиссар
	 */
	@Column(name = "commissar_id")
	Long commissarId;

	/**
	 * босс мафии
	 */
	@Column(name = "boss_mafia_id")
	Long bossMafiaId;

	/**
	 * разговоры разрешены только в первую ночь
	 * дальше false
	 */
	@Column(name = "is_first_night")
	Boolean isFirstNight;

	/**
	 * дата начала игры
	 * после окончания регистрации
	 */
	@Column(name = "start_game_at")
	LocalDateTime startGameAt;

	/**
	 * дата окончания игры
	 */
	@Column(name = "end_game_at")
	LocalDateTime endGameAt;

	/**
	 * в таблице лежат все черные. Босс мафии + члены группы
	 * Эта таблица нужны, чтобы черные могли скрытно переговариваться в первую ночь
	 */
	@Builder.Default
	@OneToMany(mappedBy = "mafiaGame", fetch = FetchType.EAGER, orphanRemoval = true)
	Set<MafiaPlayerBlack> mafiaPlayerBlacks = new HashSet<>();

	/**
	 * в таблице лежат все красные. Комиссар + жители города
	 * Эта таблица нужны, чтобы черные могли скрытно переговариваться в первую ночь
	 */
	@Builder.Default
	@OneToMany(mappedBy = "mafiaGame", fetch = FetchType.EAGER, orphanRemoval = true)
	Set<MafiaPlayerRed> mafiaPlayerReds = new HashSet<>();


}

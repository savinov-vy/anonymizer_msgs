
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
	 * шериф
	 */
	@Column(name = "sheriff_id")
	Long sheriffId;

	/**
	 * босс мафии
	 */
	@Column(name = "don_mafia_id")
	Long donMafiaId;

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
	 * в таблице лежат все красные. Шериф + жители города
	 * Эта таблица нужны, чтобы черные могли скрытно переговариваться в первую ночь
	 */
	@Builder.Default
	@OneToMany(mappedBy = "mafiaGame", fetch = FetchType.EAGER, orphanRemoval = true)
	Set<MafiaPlayerRed> mafiaPlayerReds = new HashSet<>();


}

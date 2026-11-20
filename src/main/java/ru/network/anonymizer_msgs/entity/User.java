package ru.network.anonymizer_msgs.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @Column(name = "id")
    Long id;

    /**
     * пользователь телеграмм
     */
    @Column(name = "username")
    String username;

    /**
     * внутренний ключ для бота
     */
    @Column(name = "nickname")
    String nickname;

    /**
     * город
     */
    @Column(name = "city")
    String city;

    /**
     * страна
     */
    @Column(name = "country")
    String country;

    /**
     * возраст
     */
    @Column(name = "birth_date")
    LocalDate birthDate;

    /**
     * тот кто пригласил,
     * это значение не меняется
     * нужно сделать проверку - нельзя менять если присвоено
     */
    @Column(name = "recommender_id")
    Long recommenderId;

    /**
     * сколько лайков можно поставить сегодня
     */
    @Column(name = "likes_credit")
    Integer likesCredit;

    /**
     * дата до которой премиум
     */
    @Column(name = "premium_until")
    LocalDateTime premiumUntil;

    /**
     * дата регистрации
     */
    @Column(name = "registered_at")
    LocalDateTime registeredAt;

    /**
     * дата бана пользователя
     */
    @Column(name = "banned_date")
    LocalDateTime bannedDate;

    /**
     * Пользователь заблокировал бота
     */
    @Column(name = "is_bot_blocked")
    Boolean isBotBlocked;

    /**
     * текущая просматриваемая анкета
     * поле добавляется во время отправки
     * анкеты пользователю
     */
    @Column(name = "current_reels_id")
    Long currentReelsId;

    /**
     * текущий анонимный диалог
     * для анонимных разговоров
     */
    @Column(name = "anon_dialog_id")
    Long anonDialogId;

    /**
     * текущий пользователь является анонимом
     * в текущем диалоге(аноним, владелец анонимного диалога)
     */
    @Column(name = "is_anon_owner")
    Boolean isAnonOwner;

    /**
     * последняя активность пользователя
     */
    @Column(name = "last_activity")
    LocalDateTime lastActivity;

    /**
     * пол
     */
    @Column(name = "is_man")
    Boolean isMan;

    /**
     * скрыть анкету от посторонних
     * будут видно только тем у кого пользователь
     * в белом списке. Лайкнул, но не в исключениях - не в ЧС
     * и еще не видел.
     */
    @Column(name = "is_reels_hidden")
    Boolean isReelsHidden;

    /**
     * включение/отключение приема анонимных сообщений
     */
    @Column(name = "is_anon_active")
    Boolean isAnonActive;

}

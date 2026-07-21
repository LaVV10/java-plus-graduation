package ru.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Максимальный вес действия пользователя с мероприятием (история взаимодействий).
 *
 * <p>Источник — Kafka-топик {@code stats.user-actions.v1}: Collector пишет туда каждое действие,
 * Analyzer хранит лишь максимальный вес для пары (userId, eventId) (по ТЗ — берём max, не сумму).
 * Используется для:
 * <ul>
 *   <li>исключения уже просмотренных мероприятий из рекомендаций;</li>
 *   <li>суммы максимальных весов в {@code GetInteractionsCount} (поле {@code rating} у события).</li>
 * </ul>
 */
@Entity
@Table(name = "user_action")
@IdClass(UserActionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@Id
	@Column(name = "event_id")
	private Long eventId;

	/** Максимальный вес действия (VIEW=0.4 / REGISTER=0.8 / LIKE=1.0). */
	@Column(name = "weight", nullable = false)
	private Double weight;

	/** Время последнего действия — для упорядочивания «недавно просмотренных». */
	@Column(name = "action_at")
	private Instant actionAt;
}

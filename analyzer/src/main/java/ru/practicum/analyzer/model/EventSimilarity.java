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

/**
 * Коэффициент сходства пары мероприятий (A &lt; B).
 *
 * <p>Источник — Kafka-топик {@code stats.events-similarity.v1}: Aggregator шлёт сюда
 * обновлённые значения при каждом изменении частных сумм. Analyzer upsert-ит запись
 * по композитному ключу (eventA, eventB).
 */
@Entity
@Table(name = "event_similarity")
@IdClass(EventSimilarityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarity {

	@Id
	@Column(name = "event_a")
	private Long eventA;

	@Id
	@Column(name = "event_b")
	private Long eventB;

	@Column(name = "score", nullable = false)
	private Double score;
}

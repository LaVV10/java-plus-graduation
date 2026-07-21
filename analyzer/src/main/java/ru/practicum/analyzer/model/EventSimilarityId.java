package ru.practicum.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Композитный ключ {@link EventSimilarity}: пара мероприятий (eventA, eventB), eventA &lt; eventB.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EventSimilarityId implements Serializable {
	private Long eventA;
	private Long eventB;
}

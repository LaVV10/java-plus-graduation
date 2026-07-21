package ru.practicum.analyzer.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Композитный ключ {@link UserAction}: пара (userId, eventId).
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserActionId implements Serializable {
	private Long userId;
	private Long eventId;
}

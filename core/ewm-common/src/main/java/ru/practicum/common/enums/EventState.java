package ru.practicum.common.enums;

/**
 * Состояние события. Кросс-сервисный контракт: event-service (владелец) ↔ request-service.
 */
public enum EventState {
	PENDING,
	PUBLISHED,
	CANCELED
}

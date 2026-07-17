package ru.practicum.common.enums;

/**
 * Статус заявки на участие. Кросс-сервисный контракт: request-service (владелец) ↔ event-service.
 */
public enum RequestStatus {
	PENDING,
	CONFIRMED,
	CANCELED,
	REJECTED
}

package ru.practicum.request.exception;

public class EventIsNotPublishedException extends RuntimeException {
	public EventIsNotPublishedException(String message) {
		super(message);
	}
}

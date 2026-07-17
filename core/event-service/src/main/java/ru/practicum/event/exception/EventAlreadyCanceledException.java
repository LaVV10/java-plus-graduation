package ru.practicum.event.exception;

public class EventAlreadyCanceledException extends RuntimeException {
	public EventAlreadyCanceledException(String message) {
		super(message);
	}
}

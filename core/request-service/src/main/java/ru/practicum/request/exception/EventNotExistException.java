package ru.practicum.request.exception;

public class EventNotExistException extends RuntimeException {
	public EventNotExistException(String message) {
		super(message);
	}
}

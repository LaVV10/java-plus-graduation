package ru.practicum.event.exception;

public class AlreadyPublishedException extends RuntimeException {
	public AlreadyPublishedException(String message) {
		super(message);
	}
}

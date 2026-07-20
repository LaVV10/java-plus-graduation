package ru.practicum.event.exception;

public class WrongTimeException extends RuntimeException {
	public WrongTimeException(String message) {
		super(message);
	}
}

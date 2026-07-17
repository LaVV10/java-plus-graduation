package ru.practicum.feature.exception;

public class NameAlreadyExistException extends RuntimeException {
	public NameAlreadyExistException(String message) {
		super(message);
	}
}

package ru.practicum.user.exception;

public class NameAlreadyExistException extends RuntimeException {
	public NameAlreadyExistException(String message) {
		super(message);
	}
}

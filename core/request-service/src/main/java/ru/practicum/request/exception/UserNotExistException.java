package ru.practicum.request.exception;

public class UserNotExistException extends RuntimeException {
	public UserNotExistException(String message) {
		super(message);
	}
}

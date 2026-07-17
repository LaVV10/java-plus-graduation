package ru.practicum.request.exception;

public class WrongUserException extends RuntimeException {
	public WrongUserException(String message) {
		super(message);
	}
}

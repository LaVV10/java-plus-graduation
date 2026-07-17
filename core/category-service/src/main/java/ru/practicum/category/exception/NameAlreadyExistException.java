package ru.practicum.category.exception;

public class NameAlreadyExistException extends RuntimeException {
	public NameAlreadyExistException(String message) {
		super(message);
	}
}

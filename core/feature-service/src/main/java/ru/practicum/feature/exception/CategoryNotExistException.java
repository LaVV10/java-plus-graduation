package ru.practicum.feature.exception;

public class CategoryNotExistException extends RuntimeException {
	public CategoryNotExistException(String message) {
		super(message);
	}
}

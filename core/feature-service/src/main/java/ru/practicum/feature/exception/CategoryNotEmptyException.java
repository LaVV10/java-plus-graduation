package ru.practicum.feature.exception;

public class CategoryNotEmptyException extends RuntimeException {
	public CategoryNotEmptyException(String message) {
		super(message);
	}
}

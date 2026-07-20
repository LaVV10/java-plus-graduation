package ru.practicum.category.exception;

public class CategoryNotEmptyException extends RuntimeException {
	public CategoryNotEmptyException(String message) {
		super(message);
	}
}

package ru.practicum.feature.exception;

public class CompilationNotExistException extends RuntimeException {
	public CompilationNotExistException(String message) {
		super(message);
	}
}

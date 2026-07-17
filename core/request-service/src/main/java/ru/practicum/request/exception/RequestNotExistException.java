package ru.practicum.request.exception;

public class RequestNotExistException extends RuntimeException {
	public RequestNotExistException(String message) {
		super(message);
	}
}

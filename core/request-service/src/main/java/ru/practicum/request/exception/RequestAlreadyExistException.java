package ru.practicum.request.exception;

public class RequestAlreadyExistException extends RuntimeException {
	public RequestAlreadyExistException(String message) {
		super(message);
	}
}

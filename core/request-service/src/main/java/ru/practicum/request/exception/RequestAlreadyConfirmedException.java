package ru.practicum.request.exception;

public class RequestAlreadyConfirmedException extends RuntimeException {
	public RequestAlreadyConfirmedException(String message) {
		super(message);
	}
}

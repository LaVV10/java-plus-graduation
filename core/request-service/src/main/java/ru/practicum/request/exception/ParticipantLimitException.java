package ru.practicum.request.exception;

public class ParticipantLimitException extends RuntimeException {
	public ParticipantLimitException(String message) {
		super(message);
	}
}

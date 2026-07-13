package ru.practicum.main.exception;

public class AlreadyPublishedException extends RuntimeException {
    public AlreadyPublishedException(String message) {
        super(message);
    }
}
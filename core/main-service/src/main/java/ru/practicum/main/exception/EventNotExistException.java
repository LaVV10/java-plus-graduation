package ru.practicum.main.exception;

public class EventNotExistException extends RuntimeException {
    public EventNotExistException(String message) {
        super(message);
    }
}
package ru.practicum.main.exception;

public class EventAlreadyCanceledException extends RuntimeException {
    public EventAlreadyCanceledException(String message) {
        super(message);
    }
}
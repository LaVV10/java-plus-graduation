package ru.practicum.main.exception;

public class RequestAlreadyConfirmedException extends RuntimeException {
    public RequestAlreadyConfirmedException(String message) {
        super(message);
    }
}
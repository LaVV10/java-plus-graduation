package ru.practicum.main.exception;

public class WrongUserException extends RuntimeException {
    public WrongUserException(String message) {
        super(message);
    }
}
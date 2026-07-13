package ru.practicum.main.exception;

public class WrongDataException extends RuntimeException {
    public WrongDataException(String message) {
        super(message);
    }
}
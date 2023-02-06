package ru.practicum.shareit.server.exception;

public class InvalidConditionException extends RuntimeException {
    public InvalidConditionException(String message) {
        super(message);
    }
}

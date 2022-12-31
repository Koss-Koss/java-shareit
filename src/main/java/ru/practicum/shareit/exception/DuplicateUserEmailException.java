package ru.practicum.shareit.exception;

public class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(String message) {
        super(message);
    }
}

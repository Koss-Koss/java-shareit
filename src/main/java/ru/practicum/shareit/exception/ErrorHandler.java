package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(value = {NotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(Exception exception) {
        int status_code = HttpStatus.NOT_FOUND.value();
        ErrorMessage errorMessage = new ErrorMessage(status_code, exception.getMessage());
        log.info("Ошибка запроса {} - {}", status_code, exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(value = {DuplicateUserEmailException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleDuplicateException(Exception exception) {
        int status_code = HttpStatus.CONFLICT.value();
        ErrorMessage errorMessage = new ErrorMessage(status_code, exception.getMessage());
        log.info("Ошибка запроса {} - {}", status_code, exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(value = {ForbiddenException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleForbiddenException(Exception exception) {
        int status_code = HttpStatus.FORBIDDEN.value();
        ErrorMessage errorMessage = new ErrorMessage(status_code, exception.getMessage());
        log.info("Ошибка запроса {} - {}", status_code, exception.getMessage());
        return errorMessage;
    }
}


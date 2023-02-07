package ru.practicum.shareit.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(value = {InvalidConditionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleInvalidConditionsException(Exception exception) {
        int statusCode = HttpStatus.BAD_REQUEST.value();
        ErrorMessage errorMessage = new ErrorMessage(statusCode, exception.getMessage());
        log.info("Ошибка запроса {} - {}", statusCode, exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        int statusCode = HttpStatus.BAD_REQUEST.value();
        ErrorMessage errorMessage = new ErrorMessage(statusCode, exception.getMessage());
        log.info("Ошибка запроса {} - {}", statusCode, exception.getMessage());
        return errorMessage;
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Object> handleThrowable() {
        return new ResponseEntity<>("Не удается обработать запрос", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}

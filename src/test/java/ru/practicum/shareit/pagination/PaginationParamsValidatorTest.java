package ru.practicum.shareit.pagination;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.InvalidConditionException;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.pagination.PaginationConstant.NOT_NEGATIVE_FROM_ERROR;
import static ru.practicum.shareit.pagination.PaginationConstant.NOT_POSITIVE_SIZE_ERROR;

class PaginationParamsValidatorTest {

    @Test
    void validateFromAndSize() {
        InvalidConditionException exception = assertThrows(
                InvalidConditionException.class, () -> PaginationParamsValidator.validateFromAndSize(-1, 10));
        assertEquals(NOT_NEGATIVE_FROM_ERROR, exception.getMessage());

        exception = assertThrows(
                InvalidConditionException.class, () -> PaginationParamsValidator.validateFromAndSize(10, 0));
        assertEquals(NOT_POSITIVE_SIZE_ERROR, exception.getMessage());
    }
}
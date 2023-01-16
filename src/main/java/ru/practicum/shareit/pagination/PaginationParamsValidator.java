package ru.practicum.shareit.pagination;

import ru.practicum.shareit.exception.InvalidConditionException;

import static ru.practicum.shareit.pagination.PaginationConstant.*;

public class PaginationParamsValidator {

    public static void validateFromAndSize(long from, int size) {
        if (from < 0) {
            throw new InvalidConditionException(NOT_NEGATIVE_FROM_ERROR);
        }
        if (size < 1) {
            throw new InvalidConditionException(NOT_POSITIVE_SIZE_ERROR);
        }
    }
}

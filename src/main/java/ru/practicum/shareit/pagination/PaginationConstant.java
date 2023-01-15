package ru.practicum.shareit.pagination;

import org.springframework.data.domain.Sort;

public class PaginationConstant {
    public static final String DEFAULT_PAGINATION_FROM_AS_STRING = "1";
    public static final String DEFAULT_PAGINATION_SIZE_AS_STRING = "10";
    public static final Sort DEFAULT_PAGINATION_SORT = Sort.by("id").ascending();
    public static final String NOT_POSITIVE_FROM_ERROR = "Параметр пагинации from должен быть положительным";
    public static final String NOT_POSITIVE_SIZE_ERROR = "Параметр пагинации size должен быть положительным";

}

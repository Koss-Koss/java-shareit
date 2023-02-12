package ru.practicum.shareit.server.pagination;

import org.springframework.data.domain.Sort;

public class PaginationConstant {
    public static final String DEFAULT_PAGINATION_FROM_AS_STRING = "0";
    public static final String DEFAULT_PAGINATION_SIZE_AS_STRING = "10";
    public static final Sort DEFAULT_PAGINATION_SORT = Sort.by("id").ascending();
    public static final Sort SORT_CREATED_DESC = Sort.by("created").descending();
    public static final Sort SORT_START_DESC = Sort.by("start").descending();
}

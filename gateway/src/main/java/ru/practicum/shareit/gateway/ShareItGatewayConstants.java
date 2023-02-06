package ru.practicum.shareit.gateway;

public class ShareItGatewayConstants {
    public static final String USER_REQUEST_HEADER = "X-Sharer-User-Id";
    public static final String COMMON_USER_PATH = "/users";
    public static final String COMMON_ITEM_PATH = "/items";
    public static final String COMMON_BOOKING_PATH = "/bookings";
    public static final String COMMON_ITEM_REQUEST_PATH = "/requests";
    public static final String PAGINATION_PARAMETER_FROM_NAME = "from";
    public static final String PAGINATION_PARAMETER_SIZE_NAME = "size";
    public static final String DEFAULT_PAGINATION_FROM_AS_STRING = "0";
    public static final String DEFAULT_PAGINATION_SIZE_AS_STRING = "10";
    public static final String NEGATIVE_FROM_ERROR = "Параметр пагинации from не может быть отрицательным";
    public static final String NOT_POSITIVE_SIZE_ERROR = "Параметр пагинации size должен быть положительным";
}

package ru.practicum.shareit.server.pagination;

public class PaginationUtils {
    public static int getCalculatedPage(long from, int size) {
        return Math.toIntExact(from / size);
    }
}

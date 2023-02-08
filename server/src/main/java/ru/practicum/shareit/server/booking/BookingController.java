package ru.practicum.shareit.server.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.booking.dto.*;
import ru.practicum.shareit.server.exception.InvalidConditionException;
import ru.practicum.shareit.server.pagination.PaginationUtils;

import java.util.Collection;

import static ru.practicum.shareit.server.ShareItServerConstants.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.*;

@RestController
@RequestMapping(path = COMMON_BOOKING_PATH)
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    protected static final String BOOKING_PREFIX = "/{bookingId}";
    protected static final String STATE_PREFIX = "?state=";
    protected static final String APPROVED_PREFIX = "?approved=";
    protected static final String OWNER_PATH = "/owner";
    protected static final String BOOKING_REQUEST_PARAM_APPROVED_NAME = "approved";
    private static final String BOOKING_STATE_DEFAULT = "ALL";

    @GetMapping(BOOKING_PREFIX)
    public BookingDto getByIdForUser(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @PathVariable long bookingId) {
        log.info("Получен запрос GET к эндпоинту: {}/{} от пользователя с id = {}",
                COMMON_BOOKING_PATH, bookingId, userId);
        return bookingService.findByIdForUser(userId, bookingId);
    }

    @GetMapping
    public Collection<BookingDto> getAllWithStateForUser(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @RequestParam(defaultValue = BOOKING_STATE_DEFAULT) String state,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, STATE_PREFIX, state, userId, from, size);
        return bookingService.findAllWithStateForUser(
                userId,
                BookingState.valueOf(state),
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC)
        ).getContent();
    }

    @GetMapping(OWNER_PATH)
    public Collection<BookingDto> getAllWithStateForOwner(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            @RequestParam(defaultValue = BOOKING_STATE_DEFAULT) String state,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, OWNER_PATH, STATE_PREFIX, state, ownerId, from, size);
        return bookingService.findAllWithStateForOwner(
                ownerId,
                BookingState.valueOf(state),
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC)
        ).getContent();
    }

    @PostMapping
    public BookingDto create(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @RequestBody BookingIncomingDto dto
    ) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_BOOKING_PATH, userId, dto);
        return bookingService.create(userId, dto);
    }

    @PatchMapping(BOOKING_PREFIX)
    public BookingDto setApproved(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @PathVariable long bookingId,
            @RequestParam(BOOKING_REQUEST_PARAM_APPROVED_NAME) boolean approved
    ) {
        log.info("Получен запрос PATCH к эндпоинту: {} от пользователя с id = {} со значением approved = {}",
                COMMON_BOOKING_PATH, userId, approved);
        return bookingService.setApproved(userId, bookingId, approved);
    }

    protected BookingState parseBookingState(String state) {
        try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new InvalidConditionException("Unknown state: " + state);
        }
    }

}

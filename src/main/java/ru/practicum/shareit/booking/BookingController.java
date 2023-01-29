package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.pagination.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

import static ru.practicum.shareit.ShareItAppConstants.*;
import static ru.practicum.shareit.pagination.PaginationConstant.*;

@RestController
@RequestMapping(path = COMMON_BOOKING_PATH)
@RequiredArgsConstructor
@Validated
@Slf4j
public class BookingController {

    private final BookingService bookingService;
    protected static final String BOOKING_PREFIX = "/{bookingId}";
    protected static final String STATE_PREFIX = "?state=";
    protected static final String APPROVED_PREFIX = "?approved=";
    protected static final String OWNER_PATH = "/owner";
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
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, STATE_PREFIX, state, userId, from, size);
        return bookingService.findAllWithStateForUser(
                userId,
                parseBookingState(state),
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC)
        ).getContent();
    }

    @GetMapping(OWNER_PATH)
    public Collection<BookingDto> getAllWithStateForOwner(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            @RequestParam(defaultValue = "ALL") String state,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, OWNER_PATH, STATE_PREFIX, state, ownerId, from, size);
        return bookingService.findAllWithStateForOwner(
                ownerId,
                parseBookingState(state),
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC)
        ).getContent();
    }

    @PostMapping
    public BookingDto create(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @Valid @RequestBody BookingIncomingDto dto
    ) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, userId, dto);
        return bookingService.create(userId, dto);
    }

    @PatchMapping(BOOKING_PREFIX)
    public BookingDto setApproved(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @PathVariable long bookingId,
            @RequestParam("approved") boolean approved
    ) {
        log.info("Получен запрос PATCH к эндпоинту: {} от пользователя с id = {} со значением approved = {}",
                COMMON_ITEM_PATH, userId, approved);
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

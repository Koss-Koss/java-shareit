package ru.practicum.shareit.gateway.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;
import ru.practicum.shareit.gateway.exception.InvalidConditionException;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = COMMON_BOOKING_PATH)
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    protected static final String BOOKING_PREFIX = "/{bookingId}";
    protected static final String STATE_PREFIX = "?state=";
    protected static final String OWNER_PATH = "/owner";
    protected static final String BOOKING_REQUEST_PARAM_APPROVED_NAME = "approved";
    private static final String BOOKING_STATE_DEFAULT = "ALL";

    @GetMapping(BOOKING_PREFIX)
    public ResponseEntity<Object> getByIdForUser(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @PathVariable long bookingId
    ) {
        log.info("Получен запрос GET к эндпоинту: {}/{} от пользователя с id = {}",
                COMMON_BOOKING_PATH, bookingId, userId);
        return bookingClient.get(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllWithStateForUser(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @RequestParam(defaultValue = BOOKING_STATE_DEFAULT) String state,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size
    ) {
        BookingState bookingState = parseBookingState(state);
        log.info("Получен запрос GET к эндпоинту: {}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, STATE_PREFIX, state, userId, from, size);
        return bookingClient.getCreated(userId, bookingState, from, size);
    }

    @GetMapping(OWNER_PATH)
    public ResponseEntity<Object> getForOwnedItems(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            @RequestParam(defaultValue = BOOKING_STATE_DEFAULT) String state,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size
    ) {
        /*BookingState bookingState = BookingState.from(state)
                .orElseThrow(() -> new InvalidConditionException("Unknown state: " + state));*/
        BookingState bookingState = parseBookingState(state);
        log.info("Получен запрос GET к эндпоинту: {}{}{}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_BOOKING_PATH, OWNER_PATH, STATE_PREFIX, state, ownerId, from, size);
        return bookingClient.getForOwnedItems(ownerId, bookingState, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @Valid @RequestBody BookingIncomingDto dto
    ) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_BOOKING_PATH, userId, dto);
        return bookingClient.create(userId, dto);
    }

    @PatchMapping(BOOKING_PREFIX)
    public ResponseEntity<Object> setApproved(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @PathVariable long bookingId,
            @RequestParam(BOOKING_REQUEST_PARAM_APPROVED_NAME) boolean approved
    ) {
        log.info("Получен запрос PATCH к эндпоинту: {} от пользователя с id = {} со значением approved = {}",
                COMMON_BOOKING_PATH, userId, approved);
        return bookingClient.setApproved(userId, bookingId, approved);
    }

    private BookingState parseBookingState(String state) {
        /*try {
            return BookingState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new InvalidConditionException("Unknown state: " + state);
        }*/
        return BookingState.from(state)
                .orElseThrow(() -> new InvalidConditionException("Unknown state: " + state));
    }

    /*BookingState bookingState = BookingState.from(state)
            .orElseThrow(() -> new InvalidConditionException("Unknown state: " + state));*/
}
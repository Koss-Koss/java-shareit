package ru.practicum.shareit.gateway.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.request.dto.ItemRequestIncomingDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;

@Slf4j
@RestController
@RequestMapping(path = COMMON_ITEM_REQUEST_PATH)
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;
    protected static final String ITEM_REQUEST_PREFIX = "/{requestId}";
    protected static final String ALL_PATH = "/all";

    @GetMapping(ITEM_REQUEST_PREFIX)
    public ResponseEntity<Object> getItemRequestById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                             @PathVariable long requestId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_REQUEST_PATH, requestId);
        return itemRequestClient.get(requestId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByRequesterId(@RequestHeader(USER_REQUEST_HEADER) long requesterId) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}",
                COMMON_ITEM_REQUEST_PATH, requesterId);
        return itemRequestClient.getByUserId(requesterId);
    }

    @GetMapping(ALL_PATH)
    public ResponseEntity<Object> getAllRequests(
            @RequestHeader(USER_REQUEST_HEADER) long requesterId,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_REQUEST_PATH, ALL_PATH, requesterId, from, size);
        return itemRequestClient.getAll(requesterId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addNewRequest(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                                @Valid @RequestBody ItemRequestIncomingDto itemRequestDto) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_REQUEST_PATH, userId, itemRequestDto);
        return itemRequestClient.add(userId, itemRequestDto);
    }
}

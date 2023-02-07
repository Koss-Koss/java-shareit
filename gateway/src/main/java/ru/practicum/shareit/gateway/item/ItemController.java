package ru.practicum.shareit.gateway.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.item.dto.CommentIncomingDto;
import ru.practicum.shareit.gateway.item.dto.ItemIncomingDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;

@Controller
@RequestMapping(COMMON_ITEM_PATH)
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;
    protected static final String ITEM_PREFIX = "/{itemId}";
    protected static final String SEARCH_PATH = "/search";
    protected static final String COMMENT_PATH = "/comment";

    @GetMapping(ITEM_PREFIX)
    public ResponseEntity<Object> getItemById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                              @PathVariable long itemId)  {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_PATH, itemId);
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
                @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, ownerId, from, size);
        return itemClient.getAll(ownerId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                         @Valid @RequestBody ItemIncomingDto itemDto) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, userId, itemDto);
        return itemClient.addItem(itemDto, userId);
    }

    @PatchMapping(ITEM_PREFIX)
    public ResponseEntity<Object> update(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                         @PathVariable long itemId,
                                         @RequestBody ItemIncomingDto itemDto) {
        log.info("Получен запрос PATCH к эндпоинту: {}/{} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, itemId, userId, itemDto);
        return itemClient.patchItem(itemDto, itemId, userId);
    }

    @GetMapping(SEARCH_PATH)
    public ResponseEntity<Object> getAvailableByText(
            @RequestHeader(USER_REQUEST_HEADER) long userId,
            @RequestParam String text,
            @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
            @Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}. Строка поиска: {} . " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, SEARCH_PATH, text, from, size);
        return itemClient.search(userId, text, from, size);
    }

    @PostMapping(ITEM_PREFIX + COMMENT_PATH)
    public ResponseEntity<Object> createComment(@PathVariable long itemId,
                                                @Valid @RequestBody CommentIncomingDto commentDto,
                                                @RequestHeader(USER_REQUEST_HEADER) long userId) {
        log.info("Получен запрос POST к эндпоинту: {}/{}{}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, itemId, COMMENT_PATH, commentDto);
        return itemClient.addComment(itemId, commentDto, userId);
    }

}

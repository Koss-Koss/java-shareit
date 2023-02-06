package ru.practicum.shareit.server.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.pagination.PaginationUtils;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

import static ru.practicum.shareit.server.ShareItServerConstants.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.*;

@RestController
@RequestMapping(COMMON_ITEM_PATH)
@RequiredArgsConstructor
//@Validated
@Slf4j
public class ItemController {
    private final ItemService itemService;
    protected static final String ITEM_PREFIX = "/{itemId}";
    protected static final String SEARCH_PATH = "/search";
    protected static final String SEARCH_PREFIX = "?text=";
    protected static final String COMMENT_PATH = "/comment";

    @GetMapping(ITEM_PREFIX)
    public ItemDto getItemById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                               @PathVariable long itemId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_PATH, itemId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllByOwnerId(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            //@PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            //@Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, ownerId, from, size);
        return itemService.findAllByOwnerId(
                ownerId,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT)
        ).getContent();
    }

    @PostMapping
    public ItemDto create(@RequestHeader(USER_REQUEST_HEADER) long userId,
                          /*@Valid*/ @RequestBody ItemIncomingDto itemDto) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, userId, itemDto);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping(ITEM_PREFIX)
    public ItemDto update(@RequestHeader(USER_REQUEST_HEADER) long userId,
                          @PathVariable long itemId,
                          @RequestBody ItemIncomingDto itemDto) {
        log.info("Получен запрос PATCH к эндпоинту: {}/{} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, itemId, userId, itemDto);
        return itemService.update(itemDto, itemId, userId);
    }

    @GetMapping(SEARCH_PATH)
    public Collection<ItemDto> getAvailableByText(
            @RequestParam String text,
            //@PositiveOrZero(message = NEGATIVE_FROM_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            //@Positive(message = NOT_POSITIVE_SIZE_ERROR)
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}. Строка поиска: {} . " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, SEARCH_PATH, text, from, size);
        return itemService.findAvailableByText(
                text,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT)
        ).getContent();
    }

    @PostMapping(ITEM_PREFIX + COMMENT_PATH)
    public CommentDto createComment(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                    @PathVariable long itemId,
                                    /*@Valid*/ @RequestBody CommentIncomingDto commentDto) {
        log.info("Получен запрос POST к эндпоинту: {}/{}{}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, itemId, COMMENT_PATH, commentDto);
        return itemService.createComment(userId, itemId, commentDto);
    }
}

package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.pagination.*;

import javax.validation.Valid;
import java.util.Collection;

import static ru.practicum.shareit.ShareItAppConstants.*;
import static ru.practicum.shareit.pagination.PaginationConstant.*;

@RestController
@RequestMapping(COMMON_ITEM_PATH)
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private static final String ITEM_PREFIX = "{itemId}";
    private static final String SEARCH_PATH = "/search";
    private static final String COMMENT_PATH = "/comment";

    @GetMapping(ITEM_PREFIX)
    public ItemDto getItemById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                               @PathVariable long itemId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_PATH, itemId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllByOwnerId(
            @RequestHeader(USER_REQUEST_HEADER) long ownerId,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, ownerId, from, size);
        PaginationParamsValidator.validateFromAndSize(from, size);
        return itemService.findAllByOwnerId(
                ownerId,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT)
        ).getContent();
    }

    @PostMapping
    public ItemDto create(@RequestHeader(USER_REQUEST_HEADER) long userId,
                          @Valid @RequestBody ItemIncomingDto itemDto) {
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

    @DeleteMapping(ITEM_PREFIX)
    public void delete(@RequestHeader(USER_REQUEST_HEADER) long userId,
                       @PathVariable long itemId) {
        log.info("Получен запрос DELETE к эндпоинту: {}/{} от пользователя с id = {}",
                COMMON_ITEM_PATH, itemId, userId);
        itemService.delete(itemId, userId);
    }

    @GetMapping(SEARCH_PATH)
    public Collection<ItemDto> getAvailableByText(
            @RequestParam String text,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{}. Строка поиска: {} . " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_PATH, SEARCH_PATH, text, from, size);
        PaginationParamsValidator.validateFromAndSize(from, size);
        return itemService.findAvailableByText(
                text,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT)
        ).getContent();
    }

    @PostMapping(ITEM_PREFIX + COMMENT_PATH)
    public CommentDto createComment(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentIncomingDto commentDto) {
        log.info("Получен запрос POST к эндпоинту: {}/{}{}. Данные тела запроса: {}",
                COMMON_ITEM_PATH, itemId, COMMENT_PATH, commentDto);
        return itemService.createComment(userId, itemId, commentDto);
    }
}

package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private static final String COMMON_ITEM_PATH = "/items";
    private static final String ITEM_PREFIX = "{itemId}";
    private static final String SEARCH_PREFIX = "/search";
    private static final String USER_REQUEST_HEADER = "X-Sharer-User-Id";

    @GetMapping(ITEM_PREFIX)
    public ItemDto getItemById(@PathVariable long itemId) {
        log.info("Получен запрос GET к эндпоинту: " + COMMON_ITEM_PATH + "/" + itemId);
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllByOwnerId(@RequestHeader(USER_REQUEST_HEADER) long ownerId) {
        log.info("Получен запрос GET к эндпоинту: " + COMMON_ITEM_PATH + " от пользователя с id = " + ownerId);
        return itemService.findAllByOwnerId(ownerId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader(USER_REQUEST_HEADER) long userId, @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен запрос POST к эндпоинту: " + COMMON_ITEM_PATH + " от пользователя с id = " + userId +
                ". Данные тела запроса: {}", itemDto);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping(ITEM_PREFIX)
    public ItemDto update(@RequestHeader(USER_REQUEST_HEADER) long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен запрос PATCH к эндпоинту: " + COMMON_ITEM_PATH + "/" + itemId +
                " от пользователя с id = " + userId + ". Данные тела запроса: {}", itemDto);
        return itemService.update(itemDto, itemId, userId);
    }

    @DeleteMapping(ITEM_PREFIX)
    public void delete(@RequestHeader(USER_REQUEST_HEADER) long userId,
                       @PathVariable long itemId) {
        log.info("Получен запрос DELETE к эндпоинту: " + COMMON_ITEM_PATH + "/" + itemId +
                " от пользователя с id = " + userId);
        itemService.delete(itemId, userId);
    }

    @GetMapping(SEARCH_PREFIX)
    public Collection<ItemDto> getAvailableByText(@RequestParam String text) {
        log.info("Получен запрос GET к эндпоинту: " + COMMON_ITEM_PATH + SEARCH_PREFIX +
                ". Строка поиска: {}", text);
        return itemService.findAvailableByText(text);
    }
}

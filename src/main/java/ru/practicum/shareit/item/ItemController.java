package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemIncomingDto;

import javax.validation.Valid;
import java.util.Collection;

import static ru.practicum.shareit.ShareitAppConstants.COMMON_ITEM_PATH;
import static ru.practicum.shareit.ShareitAppConstants.USER_REQUEST_HEADER;

@RestController
@RequestMapping(COMMON_ITEM_PATH)
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService itemService;
    private static final String ITEM_PREFIX = "{itemId}";
    private static final String SEARCH_PATH = "/search";

    @GetMapping(ITEM_PREFIX)
    public ItemDto getItemById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                               @PathVariable long itemId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_PATH, itemId);
        return itemService.findById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllByOwnerId(@RequestHeader(USER_REQUEST_HEADER) long ownerId) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}", COMMON_ITEM_PATH, ownerId);
        return itemService.findAllByOwnerId(ownerId);
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
                          @PathVariable Long itemId,
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
    public Collection<ItemDto> getAvailableByText(@RequestParam String text) {
        log.info("Получен запрос GET к эндпоинту: {}{}. Строка поиска: {}", COMMON_ITEM_PATH, SEARCH_PATH, text);
        return itemService.findAvailableByText(text);
    }
}

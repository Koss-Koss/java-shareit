package ru.practicum.shareit.server.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.server.pagination.PaginationUtils;
import ru.practicum.shareit.server.request.dto.ItemRequestDto;
import ru.practicum.shareit.server.request.dto.ItemRequestIncomingDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;

import java.util.Collection;
import java.util.List;

import static ru.practicum.shareit.server.ShareItServerConstants.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.*;

@RestController
@RequestMapping(COMMON_ITEM_REQUEST_PATH)
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;
    protected static final String ITEM_REQUEST_PREFIX = "/{requestId}";
    protected static final String ALL_PATH = "/all";

    @GetMapping(ITEM_REQUEST_PREFIX)
    public ItemRequestDto getItemRequestById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                             @PathVariable long requestId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_REQUEST_PATH, requestId);
        return requestService.findById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllByRequesterId(
            @RequestHeader(USER_REQUEST_HEADER) long requesterId) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}",
                COMMON_ITEM_REQUEST_PATH, requesterId);
        return requestService.findAllByRequesterId(requesterId);
    }

    @GetMapping(ALL_PATH)
    public Collection<ItemRequestDto> getAllByExpectRequesterId(
            @RequestHeader(USER_REQUEST_HEADER) long requesterId,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_REQUEST_PATH, ALL_PATH, requesterId, from, size);
        return requestService.findAllByExpectRequesterId(
                requesterId,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_CREATED_DESC)
        ).getContent();
    }

    @PostMapping
    public ItemRequestShortDto create(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                      @RequestBody ItemRequestIncomingDto itemRequestDto) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_REQUEST_PATH, userId, itemRequestDto);
        return requestService.create(itemRequestDto, userId);
    }

}

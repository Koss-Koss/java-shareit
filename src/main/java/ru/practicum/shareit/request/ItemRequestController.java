package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.pagination.*;
import ru.practicum.shareit.request.dto.*;

import javax.validation.Valid;

import java.util.Collection;

import static ru.practicum.shareit.ShareItAppConstants.*;
import static ru.practicum.shareit.pagination.PaginationConstant.*;

@RestController
@RequestMapping(COMMON_ITEM_REQUEST_PATH)
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {
    private final ItemRequestService requestService;
    private static final String REQUEST_PREFIX = "{requestId}";
    private static final String ALL_PATH = "/all";

    @GetMapping(REQUEST_PREFIX)
    public ItemRequestDto getItemRequestById(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                      @PathVariable long requestId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_ITEM_REQUEST_PATH, requestId);
        return requestService.findById(userId, requestId);
    }

    @GetMapping
    public Collection<ItemRequestDto> getAllByRequesterId(
            @RequestHeader(USER_REQUEST_HEADER) long requesterId) {
        log.info("Получен запрос GET к эндпоинту: {} от пользователя с id = {}",
                COMMON_ITEM_REQUEST_PATH, requesterId);
        return requestService.findAllByRequesterId(requesterId);
    }

    @GetMapping(ALL_PATH)
    public Collection<ItemRequestDto> getAllExpectRequesterId(
            @RequestHeader(USER_REQUEST_HEADER) long requesterId,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) long from,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        log.info("Получен запрос GET к эндпоинту: {}{} от пользователя с id = {}. " +
                        "Параметры пагинации: from = {}, size = {}",
                COMMON_ITEM_REQUEST_PATH, ALL_PATH, requesterId, from, size);
        PaginationParamsValidator.validateFromAndSize(from, size);
        return requestService.findAllExpectRequesterId(
                requesterId,
                PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT)
        ).getContent();
    }

    @PostMapping
    public ItemRequestShortDto create(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                      @Valid @RequestBody ItemRequestIncomingDto itemRequestDto) {
        log.info("Получен запрос POST к эндпоинту: {} от пользователя с id = {}. Данные тела запроса: {}",
                COMMON_ITEM_REQUEST_PATH, userId, itemRequestDto);
        return requestService.create(itemRequestDto, userId);
    }

}

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
    public ResponseEntity<Object> getId(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                        @PathVariable long itemId)  {
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                         @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
                                         @RequestParam(required = false,
                                                 defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
                                         @Positive(message = NOT_POSITIVE_SIZE_ERROR)
                                         @RequestParam(required = false,
                                                 defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        return itemClient.getAll(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addNewItem(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                             @Valid @RequestBody ItemIncomingDto itemDto) {
        return itemClient.addItem(itemDto, userId);
    }

    @PostMapping(ITEM_PREFIX + COMMENT_PATH)
    public ResponseEntity<Object> addComment(@PathVariable long itemId,
                                             @Valid @RequestBody CommentIncomingDto commentDto,
                                             @RequestHeader(USER_REQUEST_HEADER) long userId) {
        return itemClient.addComment(itemId, commentDto, userId);
    }

    @PatchMapping(ITEM_PREFIX)
    public ResponseEntity<Object> updateItem(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                             @PathVariable long itemId,
                                             @RequestBody ItemIncomingDto itemDto) {
        return itemClient.patchItem(itemDto, itemId, userId);
    }

    @GetMapping(SEARCH_PATH)
    public ResponseEntity<Object> search(@RequestHeader(USER_REQUEST_HEADER) long userId,
                                         @RequestParam String text,
                                         @PositiveOrZero(message = NEGATIVE_FROM_ERROR)
                                         @RequestParam(required = false,
                                                 defaultValue = DEFAULT_PAGINATION_FROM_AS_STRING) int from,
                                         @Positive(message = NOT_POSITIVE_SIZE_ERROR)
                                         @RequestParam(required = false,
                                                 defaultValue = DEFAULT_PAGINATION_SIZE_AS_STRING) int size) {
        return itemClient.search(userId, text, from, size);
    }
}

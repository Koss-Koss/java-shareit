package ru.practicum.shareit.gateway.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.gateway.item.dto.CommentIncomingDto;
import ru.practicum.shareit.gateway.item.dto.ItemIncomingDto;

import java.util.Map;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;
import static ru.practicum.shareit.gateway.item.ItemController.COMMENT_PATH;
import static ru.practicum.shareit.gateway.item.ItemController.SEARCH_PATH;

@Service
public class ItemClient extends BaseClient {

    private static final String SEARCH_TEXT_PREFIX = "text";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + COMMON_ITEM_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> getById(long itemId, long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAll(long userId, int from, int size) {
        Map<String, Object> param = Map.of(
                PAGINATION_PARAMETER_FROM_NAME, from,
                PAGINATION_PARAMETER_SIZE_NAME, size
        );
        return get("", userId, param);
    }

    public ResponseEntity<Object> addItem(ItemIncomingDto itemDto, long userId) {
        return post("/", userId, itemDto);
    }

    public ResponseEntity<Object> addComment(long itemId, CommentIncomingDto commentDto, long userId) {
        return post("/" + itemId + COMMENT_PATH, userId, commentDto);
    }

    public ResponseEntity<Object> patchItem(ItemIncomingDto itemDto, long itemId, long userId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> search(Long userId, String text, int from, int size) {
        Map<String, Object> param = Map.of(
                SEARCH_TEXT_PREFIX, text,
                PAGINATION_PARAMETER_FROM_NAME, from,
                PAGINATION_PARAMETER_SIZE_NAME, size
        );
        return get(SEARCH_PATH + "?" + SEARCH_TEXT_PREFIX + "={" + SEARCH_TEXT_PREFIX + "}&" +
                PAGINATION_PARAMETER_FROM_NAME + "={" + PAGINATION_PARAMETER_FROM_NAME + "}&" +
                PAGINATION_PARAMETER_SIZE_NAME + "={" + PAGINATION_PARAMETER_SIZE_NAME + "}", userId, param
        );

        //return get("/search?text={text}&from={from}&size={size}", userId, param);
    }
}

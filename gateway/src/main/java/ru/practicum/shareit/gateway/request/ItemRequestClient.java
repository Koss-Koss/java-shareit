package ru.practicum.shareit.gateway.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.client.BaseClient;
import ru.practicum.shareit.gateway.request.dto.ItemRequestIncomingDto;

import java.util.Map;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;
import static ru.practicum.shareit.gateway.request.ItemRequestController.ALL_PATH;

@Service
public class ItemRequestClient extends BaseClient {

    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + COMMON_ITEM_REQUEST_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> get(long requestId, long userId) {
        return get("/" + requestId, userId);
    }

    public ResponseEntity<Object> getByUserId(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAll(long userId, int from, int size) {
        Map<String, Object> param = Map.of(
                PAGINATION_PARAMETER_FROM_NAME, from,
                PAGINATION_PARAMETER_SIZE_NAME, size
        );
        return get(ALL_PATH + "?" + PAGINATION_PARAMETER_FROM_NAME + "={" + PAGINATION_PARAMETER_FROM_NAME + "}&" +
                PAGINATION_PARAMETER_SIZE_NAME + "={" + PAGINATION_PARAMETER_SIZE_NAME + "}", userId, param);
    }

    public ResponseEntity<Object> add(long userId, ItemRequestIncomingDto itemRequestDto) {
        return post("", userId, itemRequestDto);
    }
}

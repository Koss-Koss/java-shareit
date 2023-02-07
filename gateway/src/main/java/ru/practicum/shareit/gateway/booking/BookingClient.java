package ru.practicum.shareit.gateway.booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.gateway.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.gateway.booking.dto.BookingState;
import ru.practicum.shareit.gateway.client.BaseClient;

import java.util.Map;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.*;
import static ru.practicum.shareit.gateway.booking.BookingController.BOOKING_REQUEST_PARAM_APPROVED_NAME;
import static ru.practicum.shareit.gateway.booking.BookingController.OWNER_PATH;

@Service
public class BookingClient extends BaseClient {
    //private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + COMMON_BOOKING_PATH))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> get(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getCreated(Long userId, BookingState state, int from, int size) {
        Map<String, Object> param = Map.of(
                BOOKING_PARAMETER_STATE_NAME, state.name(),
                PAGINATION_PARAMETER_FROM_NAME, from,
                PAGINATION_PARAMETER_SIZE_NAME, size
        );
        return get("?" + BOOKING_PARAMETER_STATE_NAME + "={" + BOOKING_PARAMETER_STATE_NAME + "}&" +
                PAGINATION_PARAMETER_FROM_NAME + "={" + PAGINATION_PARAMETER_FROM_NAME + "}&" +
                PAGINATION_PARAMETER_SIZE_NAME + "={" + PAGINATION_PARAMETER_SIZE_NAME + "}", userId, param);
        //return get("?state={state}&from={from}&size={size}", userId, param);
    }

    public ResponseEntity<Object> getForOwnedItems(Long userId, BookingState state, int from, int size) {
        Map<String, Object> param = Map.of(
                BOOKING_PARAMETER_STATE_NAME, state.name(),
                PAGINATION_PARAMETER_FROM_NAME, from,
                PAGINATION_PARAMETER_SIZE_NAME, size
        );
        return get(OWNER_PATH + "?" + BOOKING_PARAMETER_STATE_NAME + "={" + BOOKING_PARAMETER_STATE_NAME + "}&" +
                PAGINATION_PARAMETER_FROM_NAME + "={" + PAGINATION_PARAMETER_FROM_NAME + "}&" +
                PAGINATION_PARAMETER_SIZE_NAME + "={" + PAGINATION_PARAMETER_SIZE_NAME + "}", userId, param);
        //return get("/owner?state={state}&from={from}&size={size}", userId, param);
    }

    public ResponseEntity<Object> create(Long userId, BookingIncomingDto bookingIncomingDto) {
        return post("", userId, bookingIncomingDto);
    }

    public ResponseEntity<Object> setApproved(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> param = Map.of(
                BOOKING_REQUEST_PARAM_APPROVED_NAME, approved
        );
        return patch("/" + bookingId + "?" + BOOKING_REQUEST_PARAM_APPROVED_NAME + "={" +
                BOOKING_REQUEST_PARAM_APPROVED_NAME + "}", userId, param, approved);
    }
}
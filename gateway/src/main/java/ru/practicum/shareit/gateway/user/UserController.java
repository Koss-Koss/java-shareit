package ru.practicum.shareit.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.dto.UserDto;

import javax.validation.Valid;

import static ru.practicum.shareit.gateway.ShareItGatewayConstants.COMMON_USER_PATH;

@Controller
@RequestMapping(path = COMMON_USER_PATH)
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;
    protected static final String USER_PREFIX = "/{userId}";

    @GetMapping(USER_PREFIX)
    public ResponseEntity<Object> getUserById(@PathVariable long userId) {
        log.info("Получен запрос GET к эндпоинту: {}/{}", COMMON_USER_PATH, userId);
        return userClient.get(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Получен запрос GET к эндпоинту: {}", COMMON_USER_PATH);
        return userClient.getAll();
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос POST к эндпоинту: {}. Данные тела запроса: {}", COMMON_USER_PATH, userDto);
        return userClient.create(userDto);
    }

    @PatchMapping(USER_PREFIX)
    public ResponseEntity<Object> update(@RequestBody UserDto userDto,
                                         @PathVariable long userId) {
        log.info("Получен запрос PATCH к эндпоинту: {}/{}. Данные тела запроса: {}",
                COMMON_USER_PATH, userId, userDto);
        return userClient.update(userId, userDto);
    }

    @DeleteMapping(USER_PREFIX)
    public ResponseEntity<Object> delete(@PathVariable long userId) {
        log.info("Получен запрос DELETE к эндпоинту: {}/{}", COMMON_USER_PATH, userId);
        return userClient.delete(userId);
    }
}

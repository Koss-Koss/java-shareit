package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private static final String COMMON_USER_PATH = "/users";
    private static final String USER_PREFIX = "{userId}";


    @GetMapping(USER_PREFIX)
    public UserDto getUserById(@PathVariable long userId) {
        log.info("Получен запрос GET к эндпоинту: " + COMMON_USER_PATH + "/" + userId);
        return userService.findById(userId);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        log.info("Получен запрос GET к эндпоинту: " + COMMON_USER_PATH);
        return userService.findAll();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Получен запрос POST к эндпоинту: " + COMMON_USER_PATH + ". Данные тела запроса: {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping(USER_PREFIX)
    public UserDto update(@RequestBody UserDto userDto, @PathVariable Long userId) {
        log.info("Получен запрос PATCH к эндпоинту: " + COMMON_USER_PATH + "/" + userId +
                ". Данные тела запроса: {}", userDto);
        return userService.update(userId, userDto);
    }

    @DeleteMapping(USER_PREFIX)
    public void delete(@PathVariable long userId) {
        log.info("Получен запрос DELETE к эндпоинту: " + COMMON_USER_PATH + "/" + userId);
        userService.delete(userId);
    }
}

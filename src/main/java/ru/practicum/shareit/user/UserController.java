package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("{userId}")
    public UserDto getUserById(@PathVariable long userId) {
        return userService.findById(userId);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto, @PathVariable Long userId) {
        return userService.update(userId, userDto);
    }

    @DeleteMapping("{userId}")
    public void delete(@PathVariable long userId) {
        userService.delete(userId);
    }
}

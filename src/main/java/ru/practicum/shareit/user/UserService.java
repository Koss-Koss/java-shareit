package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto findById(long id);

    Collection<UserDto> findAll();

    UserDto create(UserDto userDto);

    UserDto update(long id, UserDto userDto);

    void delete(long id);
}

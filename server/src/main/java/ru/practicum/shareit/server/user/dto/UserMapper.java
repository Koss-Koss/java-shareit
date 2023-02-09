package ru.practicum.shareit.server.user.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.server.user.model.User;

@UtilityClass
public class UserMapper {
    public static UserDto toUserDto(User userStorage) {
        return UserDto.builder()
                .id(userStorage.getId())
                .name(userStorage.getName())
                .email(userStorage.getEmail())
                .build();
    }

    public static User toUser(UserDto userDto) {
        return User.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}

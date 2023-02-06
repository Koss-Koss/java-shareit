package ru.practicum.shareit.gateway.user.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

//@Data
@RequiredArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    @NotEmpty(message = "Не указано имя (name) пользователя")
    String name;
    @NotEmpty(message = "Не указан email пользователя")
    @Email(message = "Указан некорректный email пользователя")
    String email;
}

package ru.practicum.shareit.gateway.user.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

//@Data
//@RequiredArgsConstructor
@Getter
public class UserDto {
    Long id;
    @NotEmpty(message = "Не указано имя (name) пользователя")
    String name;
    @NotEmpty(message = "Не указан email пользователя")
    @Email(message = "Указан некорректный email пользователя")
    String email;
}

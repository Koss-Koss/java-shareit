package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
@Builder
public class UserDto {
    Long id;
    @NotBlank(message = "Не указано имя (name) пользователя")
    String name;
    @NotBlank(message = "Не указан email пользователя")
    @Email(message = "Указан некорректный email пользователя")
    String email;
}

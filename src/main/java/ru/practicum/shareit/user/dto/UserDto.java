package ru.practicum.shareit.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    @NotBlank(message = "Не указано имя (name) пользователя")
    String name;
    @NotBlank(message = "Не указан email пользователя")
    @Email(message = "Указан некорректный email пользователя")
    String email;
}

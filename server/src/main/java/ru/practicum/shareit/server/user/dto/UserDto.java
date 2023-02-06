package ru.practicum.shareit.server.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    //@NotBlank(message = "Не указано имя (name) пользователя")
    String name;
    //@NotBlank(message = "Не указан email пользователя")
    //@Email(message = "Указан некорректный email пользователя")
    String email;
}

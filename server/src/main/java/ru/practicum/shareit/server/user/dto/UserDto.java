package ru.practicum.shareit.server.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    //@NotBlank(message = "Не указано имя (name) пользователя")
    String name;
    //@NotBlank(message = "Не указан email пользователя")
    //@Email(message = "Указан некорректный email пользователя")
    String email;
}

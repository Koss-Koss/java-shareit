package ru.practicum.shareit.gateway.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@RequiredArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class UserDto {
    Long id;
    @NotEmpty(message = "Не указано имя (name) пользователя")
    String name;
    @NotEmpty(message = "Не указан email пользователя")
    @Email(message = "Указан некорректный email пользователя")
    String email;
}

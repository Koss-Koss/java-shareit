package ru.practicum.shareit.server.user.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    String name;
    String email;
}

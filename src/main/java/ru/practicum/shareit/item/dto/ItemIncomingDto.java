package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemIncomingDto {
    @NotBlank(message = "Не указано название (name) вещи")
    String name;
    @NotBlank(message = "Не указано описание (description) вещи")
    String description;
    @NotNull(message = "Не заполнен статус доступности для аренды (available) вещи")
    Boolean available;
}

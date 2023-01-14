package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemIncomingDto {
    @NotBlank(message = "Не указано название (name) вещи")
    String name;
    @NotBlank(message = "Не указано описание (description) вещи")
    String description;
    @NotNull(message = "Не заполнен статус доступности для аренды (available) вещи")
    Boolean available;
}

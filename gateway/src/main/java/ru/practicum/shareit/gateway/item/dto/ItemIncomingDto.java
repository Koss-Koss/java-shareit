package ru.practicum.shareit.gateway.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@Getter
//@Builder(toBuilder = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
public class ItemIncomingDto {
    @NotBlank(message = "Не указано название (name) вещи")
    String name;
    @NotBlank(message = "Не указано описание (description) вещи")
    String description;
    @NotNull(message = "Не заполнен статус доступности для аренды (available) вещи")
    Boolean available;
    @Positive(message = "id запроса вещи должен быть положительным числом")
    Long requestId;
}

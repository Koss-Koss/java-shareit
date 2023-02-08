package ru.practicum.shareit.server.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemIncomingDto {
    String name;
    String description;
    Boolean available;
    Long requestId;
}

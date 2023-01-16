package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemForItemRequestDto {
    Long id;
    String name;
    String description;
    Long ownerId;
    Boolean available;
    Long requestId;
}

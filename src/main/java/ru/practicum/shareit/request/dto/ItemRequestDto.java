package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ItemRequestDto {
    Long id;
    String description;
    Long requesterId;
    LocalDateTime created;
}

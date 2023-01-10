package ru.practicum.shareit.request.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Value
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ItemRequest {
    Long id;
    String description;
    Long requesterId;
    LocalDateTime created;
}

package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class ItemRequest {
    Long id;
    String description;
    Long requesterId;
    LocalDateTime created;
}

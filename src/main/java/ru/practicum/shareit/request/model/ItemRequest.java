package ru.practicum.shareit.request.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ItemRequest {
    Long id;
    String description;
    Long requesterId;
    LocalDateTime created;
}

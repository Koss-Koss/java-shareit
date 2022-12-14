package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class ItemRequestDto {
    Long id;
    String description;
    Long requesterId;
    LocalDateTime created;
}

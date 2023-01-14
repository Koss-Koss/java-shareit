package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder
public class CommentDto {
    Long id;
    String text;
    String authorName;
    LocalDateTime created;
}

package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(exclude = {"comments"})
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoForNotOwner {
    Long id;
    String name;
    String description;
    Boolean available;
    @Setter
    Collection<CommentDto> comments;
    Long requestId;

}

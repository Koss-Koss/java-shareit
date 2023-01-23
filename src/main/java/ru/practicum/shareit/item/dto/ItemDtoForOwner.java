package ru.practicum.shareit.item.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(exclude = {"comments"})
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDtoForOwner {
    Long id;
    String name;
    String description;
    Boolean available;
    @Setter
    BookingShortDto lastBooking;
    @Setter
    BookingShortDto nextBooking;
    @Setter
    Collection<CommentDto> comments;
    Long requestId;

}
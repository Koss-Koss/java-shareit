package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

@Data
@Builder
public class ItemDto {
    Long id;
    String name;
    String description;
    Boolean available;
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
    Collection<CommentDto> comments;
}

package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.Collection;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    @Setter
    private BookingShortDto lastBooking;
    @Setter
    private BookingShortDto nextBooking;
    @Setter
    private Collection<CommentDto> comments;
}

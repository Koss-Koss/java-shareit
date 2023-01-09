package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingShortDto {
    Long id;
    Long bookerId;
}

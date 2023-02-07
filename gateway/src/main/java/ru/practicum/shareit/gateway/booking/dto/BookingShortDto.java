package ru.practicum.shareit.gateway.booking.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@EqualsAndHashCode
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class BookingShortDto {
    Long id;
    Long bookerId;
}

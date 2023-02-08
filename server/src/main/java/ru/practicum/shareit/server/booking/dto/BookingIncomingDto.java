package ru.practicum.shareit.server.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BookingIncomingDto {
    Long itemId;
    LocalDateTime start;
    LocalDateTime end;
}
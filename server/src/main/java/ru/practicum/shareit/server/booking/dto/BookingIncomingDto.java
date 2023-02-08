package ru.practicum.shareit.server.booking.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.server.booking.dto.validation.BookingPeriodValidation;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@BookingPeriodValidation(message = "Окончание бронирования должно быть позже его начала")
public class BookingIncomingDto {
    //@NotNull
    Long itemId;
    //@FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    LocalDateTime start;
    //@FutureOrPresent(message = "Окончание бронирования не может быть в прошлом")
    LocalDateTime end;
}
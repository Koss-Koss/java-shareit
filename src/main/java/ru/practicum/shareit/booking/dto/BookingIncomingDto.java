package ru.practicum.shareit.booking.dto;

import lombok.Value;
import ru.practicum.shareit.booking.dto.validation.BookingPeriodValidation;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Value
@BookingPeriodValidation(message = "Окончание бронирования должно быть позже его начала")
public class BookingIncomingDto {
    @NotNull
    Long itemId;
    @FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    LocalDateTime start;
    @FutureOrPresent(message = "Окончание бронирования не может быть в прошлом")
    LocalDateTime end;
}

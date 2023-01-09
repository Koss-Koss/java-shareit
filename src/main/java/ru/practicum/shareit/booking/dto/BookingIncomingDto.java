package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.dto.validation.BookingPeriodValidation;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@BookingPeriodValidation(message = "Окончание бронирования должно быть позже его начала")
public class BookingIncomingDto {
    @NotNull
    private Long itemId;
    @FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    private LocalDateTime start;
    @FutureOrPresent(message = "Окончание бронирования не может быть в прошлом")
    private LocalDateTime end;
}

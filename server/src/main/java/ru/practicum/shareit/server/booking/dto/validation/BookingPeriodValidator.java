package ru.practicum.shareit.server.booking.dto.validation;

import ru.practicum.shareit.server.booking.dto.BookingIncomingDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class BookingPeriodValidator implements ConstraintValidator<BookingPeriodValidation, BookingIncomingDto> {

    @Override
    public boolean isValid(BookingIncomingDto bookingDto, ConstraintValidatorContext context) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        return end.isAfter(start);
    }
}
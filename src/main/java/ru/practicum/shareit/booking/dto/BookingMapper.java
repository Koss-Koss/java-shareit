package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;

@Component
public class BookingMapper {
    public BookingDto toBookingDto(Booking bookingStorage) {
        return BookingDto.builder()
                .id(bookingStorage.getId())
                .start(bookingStorage.getStart())
                .end(bookingStorage.getEnd())
                .itemId(bookingStorage.getItemId())
                .bookerId(bookingStorage.getBookerId())
                .status(bookingStorage.getStatus())
                .build();
    }

    public Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .itemId(bookingDto.getItemId())
                .bookerId(bookingDto.getBookerId())
                .status(bookingDto.getStatus())
                .build();
    }
}

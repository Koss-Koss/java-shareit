package ru.practicum.shareit.server.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.item.dto.ItemMapper;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.model.User;

@UtilityClass
public class BookingMapper {

    public static BookingDto toBookingDto(Booking bookingStorage) {
        return BookingDto.builder()
                .id(bookingStorage.getId())
                .start(bookingStorage.getStart())
                .end(bookingStorage.getEnd())
                .item(ItemMapper.toItemDto(bookingStorage.getItem()))
                .booker(UserMapper.toUserDto(bookingStorage.getBooker()))
                .status(bookingStorage.getStatus())
                .build();
    }

    public static BookingShortDto toBookingShortDto(Booking bookingStorage) {
        if (bookingStorage == null) return null;
        return BookingShortDto.builder()
                .id(bookingStorage.getId())
                .bookerId(bookingStorage.getBooker().getId())
                .build();
    }

    public static Booking toBooking(BookingIncomingDto bookingDto, Item item, User booker) {
        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
    }
}

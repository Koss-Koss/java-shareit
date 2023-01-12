package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.util.Collection;

public interface BookingService {

    BookingDto findByIdForUser(long userId, long id);

    Collection<BookingDto> findAllWithStateForUser(long userId, BookingState state);

    Collection<BookingDto> findAllWithStateForOwner(long ownerId, BookingState state);

    BookingDto create(long userId, BookingIncomingDto bookingDto);

    BookingDto setApproved(long ownerId, long id, boolean approved);

}

package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.dto.BookingState;

public interface BookingService {

    BookingDto findByIdForUser(long userId, long id);

    Page<BookingDto> findAllWithStateForUser(long userId, BookingState state, Pageable pageable);

    Page<BookingDto> findAllWithStateForOwner(long ownerId, BookingState state, Pageable pageable);

    BookingDto create(long userId, BookingIncomingDto bookingDto);

    BookingDto setApproved(long ownerId, long id, boolean approved);

}

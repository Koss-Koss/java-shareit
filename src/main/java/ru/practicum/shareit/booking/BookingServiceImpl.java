package ru.practicum.shareit.booking;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class BookingServiceImpl implements BookingService {
    ItemRepository itemRepository;
    BookingRepository bookingRepository;
    UserRepository userRepository;

    @Override
    public BookingDto findByIdForUser(long userId, long bookingId) {
        User user = userRepository.extract(userId);
        Booking booking = bookingRepository.extract(bookingId);

        if (!booking.getBooker().equals(user) && !booking.getItem().getOwner().equals(user)) {
            throw new NotFoundException("Невозможно получить бронирование id = " + bookingId +
                    " для данного пользователя");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public Page<BookingDto> findAllWithStateForUser(long userId, BookingState state, Pageable pageable) {
        userRepository.extract(userId);
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> result;
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case WAITING:
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED, pageable);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentForBooker(userId, now, pageable);
                break;
            case PAST:
                result = bookingRepository.findAllByBookerIdAndEndLessThanOrderByStartDesc(
                        userId, now, pageable);
                break;
            case FUTURE:
                result = bookingRepository.findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                        userId, now, pageable);
                break;
            default:
                throw new InvalidConditionException("Unknown state: " + state);
        }
        return result
                .map(BookingMapper::toBookingDto);
    }

    @Override
    public Page<BookingDto> findAllWithStateForOwner(long ownerId, BookingState state, Pageable pageable) {
        userRepository.extract(ownerId);
        LocalDateTime now = LocalDateTime.now();
        Page<Booking> result;
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId, pageable);
                break;
            case WAITING:
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.WAITING, pageable);
                break;
            case REJECTED:
                result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                        ownerId, BookingStatus.REJECTED, pageable);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentForOwner(ownerId, now, pageable);
                break;
            case PAST:
                result = bookingRepository.findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                        ownerId, now, pageable);
                break;
            case FUTURE:
                result = bookingRepository.findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                        ownerId, now, pageable);
                break;
            default:
                throw new InvalidConditionException("Unknown state: " + state);
        }
        return result
                .map(BookingMapper::toBookingDto);
    }

    @Transactional
    @Override
    public BookingDto create(long userId, BookingIncomingDto dto) {
        User booker = userRepository.extract(userId);
        Item item = itemRepository.extract(dto.getItemId());

        if (item.getOwner().equals(booker)) {
            throw new NotFoundException("Запрещено бронировать свои вещи: пользователь с id = " + userId +
                    " является владельцем вещи с id = " + item.getId());
        }
        if (!item.getAvailable()) {
            throw new InvalidConditionException("Вещь с id = " + item.getId() + " недоступна для бронирования");
        }

        Booking newBooking = BookingMapper.toBooking(dto, item, booker);
        Booking createdBooking = bookingRepository.save(newBooking);
        log.info("Добавлено бронирование с id = {} для вещи с id = {} владельца с id = {}",
                createdBooking.getId(), item.getId(), userId);
        return BookingMapper.toBookingDto(createdBooking);
    }

    @Transactional
    @Override
    public BookingDto setApproved(long userId, long id, boolean approved) {
        userRepository.extract(userId);
        Booking booking = bookingRepository.extract(id);

        if (booking.getItem().getOwner().getId() != userId) {
            throw new NotFoundException("Запрещено одобрять бронирование чужих вещей: пользователь с id = " + userId +
                    " не является владельцем вещи с id = " + booking.getItem().getId() +
                    " из бронирования с id = " + id);
        }
        if (booking.getStatus() == BookingStatus.APPROVED) {
            throw new InvalidConditionException("Бронирование с id = " + id + " уже одобрено");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Владельцем (пользователь id = {}) вещи в бронировании с id = {} изменён статус бронирования на {}",
                userId, id, updatedBooking.getStatus());
        return BookingMapper.toBookingDto(updatedBooking);
    }

}

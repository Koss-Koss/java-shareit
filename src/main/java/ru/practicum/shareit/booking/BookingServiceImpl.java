package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

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
    public Collection<BookingDto> findAllWithStateForUser(long userId, BookingState state) {
        userRepository.extract(userId);
        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> result;
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByBookerId(userId);
                break;
            case WAITING:
                result = bookingRepository.findAllByStatusForBooker(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                result = bookingRepository.findAllByStatusForBooker(userId, BookingStatus.REJECTED);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentForBooker(userId, now);
                break;
            case PAST:
                result = bookingRepository.findAllPastForBooker(userId, now);
                break;
            case FUTURE:
                result = bookingRepository.findAllFutureForBooker(userId, now);
                break;
            default:
                throw new InvalidConditionException("Unknown state: " + state);
        }
        return Optional.ofNullable(result)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<BookingDto> findAllWithStateForOwner(long ownerId, BookingState state) {
        userRepository.extract(ownerId);
        if (itemRepository.findFirstByOwnerId(ownerId).isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();
        Collection<Booking> result;
        switch (state) {
            case ALL:
                result = bookingRepository.findAllByOwnerId(ownerId);
                break;
            case WAITING:
                result = bookingRepository.findAllByStatusForOwner(ownerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                result = bookingRepository.findAllByStatusForOwner(ownerId, BookingStatus.REJECTED);
                break;
            case CURRENT:
                result = bookingRepository.findAllCurrentForOwner(ownerId, now);
                break;
            case PAST:
                result = bookingRepository.findAllPastForOwner(ownerId, now);
                break;
            case FUTURE:
                result = bookingRepository.findAllFutureForOwner(ownerId, now);
                break;
            default:
                throw new InvalidConditionException("Unknown state: " + state);
        }
        return Optional.ofNullable(result)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
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

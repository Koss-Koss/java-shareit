package ru.practicum.shareit.server.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.server.booking.model.Booking;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.NotFoundException;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    default Booking extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующее бронирование с id = " + id));
    }

    Page<Booking> findAllByBookerId(long bookerId, Pageable pageable);

    Page<Booking> findAllByItemOwnerId(long ownerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatus(
            long bookerId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStatus(
            long ownerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start <= ?2 and b.end >= ?2")
    Page<Booking> findAllCurrentForBooker(long bookerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start <= ?2 and b.end >= ?2")
    Page<Booking> findAllCurrentForOwner(long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndLessThan(
            long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndLessThan(
            long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartGreaterThan(
            long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartGreaterThan(
            long ownerId, LocalDateTime now, Pageable pageable);

    Booking findFirstByItemIdAndEndLessThanOrderByStartDesc(
            long itemId, LocalDateTime now);

    Booking findFirstByItemIdAndStartGreaterThanOrderByStartAsc(
            long itemId, LocalDateTime now);

    @Query("select count (b) from Booking b " +
            "where b.booker.id = ?1 " +
            "and b.item.id = ?2 " +
            "and b.end < ?3 " +
            "and b.status = ru.practicum.shareit.server.booking.model.BookingStatus.APPROVED")
    Integer countCompletedBookings(long bookerId, long itemId, LocalDateTime now);

}

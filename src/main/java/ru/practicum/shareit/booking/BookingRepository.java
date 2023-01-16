package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    default Booking extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующее бронирование с id = " + id));
    }

    Page<Booking> findAllByBookerIdOrderByStartDesc(long bookerId, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdOrderByStartDesc(long ownerId, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(
            long bookerId, BookingStatus status, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(
            long ownerId, BookingStatus status, Pageable pageable);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start <= ?2 and b.end >= ?2 " +
            "order by b.start desc")
    Page<Booking> findAllCurrentForBooker(long bookerId, LocalDateTime now, Pageable pageable);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start <= ?2 and b.end >= ?2 " +
            "order by b.start desc")
    Page<Booking> findAllCurrentForOwner(long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndEndLessThanOrderByStartDesc(
            long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
            long ownerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
            long bookerId, LocalDateTime now, Pageable pageable);

    Page<Booking> findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
            long ownerId, LocalDateTime now, Pageable pageable);

    Booking findByItemIdAndEndLessThanOrderByStartDesc(
            long itemId, LocalDateTime now);

    Booking findByItemIdAndStartGreaterThanOrderByStartDesc(
            long itemId, LocalDateTime now);

    @Query("select count (b) from Booking b " +
            "where b.booker.id = ?1 " +
                "and b.item.id = ?2 " +
                "and b.end < ?3 " +
                "and b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED")
    Integer countCompletedBookings(long bookerId, long itemId, LocalDateTime now);

}

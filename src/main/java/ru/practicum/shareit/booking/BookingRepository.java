package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    default Booking extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующее бронирование с id = " + id));
    }

    @Query("select b from Booking b where b.id >= ?2 and b.booker.id = ?1 order by b.start desc")
    Page<Booking> findAllByBookerId(long bookerId, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?2 and b.item.owner.id = ?1 order by b.start desc")
    Page<Booking> findAllByOwnerId(long ownerId, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.booker.id = ?1 and b.status = ?2 order by b.start desc")
    Page<Booking> findAllByStatusForBooker(long bookerId, BookingStatus status, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.item.owner.id = ?1 and b.status = ?2 order by b.start desc")
    Page<Booking> findAllByStatusForOwner(long ownerId, BookingStatus status, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.booker.id = ?1 and b.start <= ?2 and b.end >= ?2 " +
            "order by b.start desc")
    Page<Booking> findAllCurrentForBooker(long bookerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.item.owner.id = ?1 and b.start <= ?2 and b.end >= ?2 " +
            "order by b.start desc")
    Page<Booking> findAllCurrentForOwner(long ownerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.booker.id = ?1 and b.end < ?2 order by b.start desc")
    Page<Booking> findAllPastForBooker(long bookerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.item.owner.id = ?1 and b.end < ?2 order by b.start desc")
    Page<Booking> findAllPastForOwner(long ownerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.booker.id = ?1 and b.start > ?2 order by b.start desc")
    Page<Booking> findAllFutureForBooker(long bookerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.id >= ?3 and b.item.owner.id = ?1 and b.start > ?2 order by b.start desc")
    Page<Booking> findAllFutureForOwner(long ownerId, LocalDateTime now, long from, Pageable pageable);

    @Query("select b from Booking b where b.item.id = ?1 and b.end < ?2 order by b.start desc")
    Booking findLastForItem(long itemId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = ?1 and b.start > ?2 order by b.start desc")
    Booking findNextForItem(long itemId, LocalDateTime now);

    @Query("select count (b) from Booking b " +
            "where b.booker.id = ?1 " +
                "and b.item.id = ?2 " +
                "and b.end < ?3 " +
                "and b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED")
    Integer countCompletedBookings(long bookerId, long itemId, LocalDateTime now);

}

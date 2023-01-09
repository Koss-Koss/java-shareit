package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    default Booking extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующее бронирование с id = " + id));
    }

    @Query("select b from Booking b where b.booker.id = ?1 order by b.start desc")
    Collection<Booking> findAllByBookerId(long bookerId);

    @Query("select b from Booking b where b.item.owner.id = ?1 order by b.start desc")
    Collection<Booking> findAllByOwnerId(long ownerId);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2 order by b.start desc")
    Collection<Booking> findAllByStatusForBooker(long bookerId, BookingStatus status);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2 order by b.start desc")
    Collection<Booking> findAllByStatusForOwner(long ownerId, BookingStatus status);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start <= ?2 and b.end >= ?2 order by b.start desc")
    Collection<Booking> findAllCurrentForBooker(long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start <= ?2 and b.end >= ?2 order by b.start desc")
    Collection<Booking> findAllCurrentForOwner(long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.end < ?2 order by b.start desc")
    Collection<Booking> findAllPastForBooker(long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.end < ?2 order by b.start desc")
    Collection<Booking> findAllPastForOwner(long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.booker.id = ?1 and b.start > ?2 order by b.start desc")
    Collection<Booking> findAllFutureForBooker(long bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.start > ?2 order by b.start desc")
    Collection<Booking> findAllFutureForOwner(long ownerId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = ?1 and b.end < ?2 order by b.start desc")
    Booking findLastForItem(long itemId, LocalDateTime now);

    @Query("select b from Booking b where b.item.id = ?1 and b.start > ?2 order by b.start desc")
    Booking findNextForItem(long itemId, LocalDateTime now);

}

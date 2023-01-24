package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    int from = 1;
    int size = 10;
    User saveBooker;
    User saveOwner;
    Item saveItem;
    Item saveItem2;
    Booking saveBooking;
    Booking saveBooking2;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = start.plusMinutes(10);

    @BeforeEach
    private void addItems() {
        saveBooker = userRepository.save(User.builder().name("BookerName").email("booker@test.com").build());
        saveOwner = userRepository.save(User.builder().name("OwnerName").email("owner@test.com").build());
        saveItem = itemRepository.save(Item.builder()
                .name("TestItemName")
                .description("ItemDescription")
                .available(true)
                .owner(saveOwner)
                .build());
        saveItem2 = itemRepository.save(Item.builder()
                .name("TestItemName2")
                .description("ItemDescription2")
                .available(true)
                .owner(saveOwner)
                .build());
        saveBooking = bookingRepository.save(Booking.builder()
                .start(start)
                .end(end)
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        saveBooking2 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(60))
                .end(end.plusMinutes(60))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
    }

    @Test
    void extract() {
        assertEquals(saveBooking, bookingRepository.extract(saveBooking.getId()));
        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingRepository.extract(saveBooking.getId() + 100));
        assertEquals("Запрос на несуществующее бронирование с id = " + (saveBooking.getId() + 100), exception.getMessage());
    }

    @Test
    void findAllByBookerIdOrderByStartDesc() {
        Page<Booking> result = bookingRepository.findAllByBookerIdOrderByStartDesc(saveBooker.getId(), pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdOrderByStartDesc(
                saveOwner.getId(), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start)
                .end(end)
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(saveOwner.getId(), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdOrderByStartDesc(
                saveOwner.getId() + 100, pageable).getContent().size());
    }

    @Test
    void findAllByBookerIdAndStatusOrderByStartDesc() {
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(120))
                .end(end.plusMinutes(120))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        saveBooking2.setStatus(BookingStatus.APPROVED);

        Page<Booking> result = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                saveBooker.getId(), BookingStatus.WAITING, pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                saveOwner.getId() + 100, BookingStatus.WAITING, pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                saveBooker.getId(), BookingStatus.REJECTED, pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndStatusOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(120))
                .end(end.plusMinutes(120))
                .item(saveItem3)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(120))
                .end(end.plusMinutes(120))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.APPROVED)
                .build());
        saveBooking2.setStatus(BookingStatus.APPROVED);

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                saveOwner.getId(), BookingStatus.APPROVED, pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                saveOwner.getId() + 100, BookingStatus.WAITING, pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                saveOwner.getId(), BookingStatus.REJECTED, pageable).getContent().size());
    }

    @Test
    void findAllCurrentForBooker() {
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllCurrentForBooker(
                saveBooker.getId(), start.plusMinutes(65), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking3, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllCurrentForBooker(
                saveOwner.getId(), start.plusMinutes(65), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllCurrentForBooker(
                saveBooker.getId(), start.plusMinutes(40), pageable).getContent().size());
    }

    @Test
    void findAllCurrentForOwner() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem3)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllCurrentForOwner(
                saveOwner.getId(), start.plusMinutes(65), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking3, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllCurrentForOwner(
                saveBooker.getId(), start.plusMinutes(200), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllCurrentForOwner(
                saveOwner.getId(), start.plusMinutes(200), pageable).getContent().size());
    }

    @Test
    void findAllByBookerIdAndEndLessThanOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllByBookerIdAndEndLessThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(80), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndEndLessThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(50), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndEndLessThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(5), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndEndLessThanOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(80), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(50), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(5), pageable).getContent().size());
    }

    @Test
    void findAllByBookerIdAndStartGreaterThanOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(40), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(80), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(200), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(50))
                .end(end.plusMinutes(70))
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.WAITING)
                .build());
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(40), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                saveBooker.getId(), start.plusMinutes(80), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                saveOwner.getId(), start.plusMinutes(200), pageable).getContent().size());
    }

    @Test
    void findFirstByItemIdAndEndLessThanOrderByStartDesc() {
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(150))
                .end(end.plusMinutes(150))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        assertEquals(saveBooking3, bookingRepository.findFirstByItemIdAndEndLessThanOrderByStartDesc(
                saveItem2.getId(), start.plusMinutes(130)));
        assertNull(bookingRepository.findFirstByItemIdAndEndLessThanOrderByStartDesc(
                saveItem.getId(), start.plusMinutes(5)));
    }

    @Test
    void findFirstByItemIdAndStartGreaterThanOrderByStartAsc() {
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(150))
                .end(end.plusMinutes(150))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());

        assertEquals(saveBooking2, bookingRepository.findFirstByItemIdAndStartGreaterThanOrderByStartAsc(
                saveItem2.getId(), start.plusMinutes(50)));
        assertNull(bookingRepository.findFirstByItemIdAndStartGreaterThanOrderByStartAsc(
                saveItem.getId(), start.plusMinutes(10)));
    }

    @Test
    void countCompletedBookings() {
        Item saveItem3 = itemRepository.save(Item.builder()
                .name("TestItemName3")
                .description("ItemDescription3")
                .available(true)
                .owner(saveBooker)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(100))
                .end(end.plusMinutes(100))
                .item(saveItem3)
                .booker(saveOwner)
                .status(BookingStatus.APPROVED)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(150))
                .end(end.plusMinutes(150))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.APPROVED)
                .build());
        bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(200))
                .end(end.plusMinutes(200))
                .item(saveItem2)
                .booker(saveBooker)
                .status(BookingStatus.APPROVED)
                .build());
        assertEquals(1, bookingRepository.countCompletedBookings(
                saveBooker.getId(), saveItem2.getId(), start.plusMinutes(170)));
    }

    @AfterEach
    private void deleteItems() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        bookingRepository.deleteAll();
    }
}
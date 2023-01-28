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
import static ru.practicum.shareit.pagination.PaginationConstant.SORT_START_DESC;

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
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC);
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
    void findAllByBookerId() {
        Page<Booking> result = bookingRepository.findAllByBookerId(saveBooker.getId(), pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerId(
                saveOwner.getId(), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerId() {
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

        Page<Booking> result = bookingRepository.findAllByItemOwnerId(saveOwner.getId(), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerId(
                saveOwner.getId() + 100, pageable).getContent().size());
    }

    @Test
    void findAllByBookerIdAndStatus() {
        Booking saveBooking3 = bookingRepository.save(Booking.builder()
                .start(start.plusMinutes(120))
                .end(end.plusMinutes(120))
                .item(saveItem)
                .booker(saveBooker)
                .status(BookingStatus.WAITING)
                .build());
        saveBooking2.setStatus(BookingStatus.APPROVED);

        Page<Booking> result = bookingRepository.findAllByBookerIdAndStatus(
                saveBooker.getId(), BookingStatus.WAITING, pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndStatus(
                saveOwner.getId() + 100, BookingStatus.WAITING, pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndStatus(
                saveBooker.getId(), BookingStatus.REJECTED, pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndStatus() {
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

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndStatus(
                saveOwner.getId(), BookingStatus.APPROVED, pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStatus(
                saveOwner.getId() + 100, BookingStatus.WAITING, pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStatus(
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
    void findAllByBookerIdAndEndLessThan() {
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

        Page<Booking> result = bookingRepository.findAllByBookerIdAndEndLessThan(
                saveBooker.getId(), start.plusMinutes(80), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndEndLessThan(
                saveOwner.getId(), start.plusMinutes(50), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndEndLessThan(
                saveBooker.getId(), start.plusMinutes(5), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndEndLessThan() {
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

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndEndLessThan(
                saveOwner.getId(), start.plusMinutes(80), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking2, result.getContent().get(0));
        assertEquals(saveBooking, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndEndLessThan(
                saveBooker.getId(), start.plusMinutes(50), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndEndLessThan(
                saveOwner.getId(), start.plusMinutes(5), pageable).getContent().size());
    }

    @Test
    void findAllByBookerIdAndStartGreaterThan() {
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

        Page<Booking> result = bookingRepository.findAllByBookerIdAndStartGreaterThan(
                saveBooker.getId(), start.plusMinutes(40), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByBookerIdAndStartGreaterThan(
                saveOwner.getId(), start.plusMinutes(80), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByBookerIdAndStartGreaterThan(
                saveBooker.getId(), start.plusMinutes(200), pageable).getContent().size());
    }

    @Test
    void findAllByItemOwnerIdAndStartGreaterThan() {
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

        Page<Booking> result = bookingRepository.findAllByItemOwnerIdAndStartGreaterThan(
                saveOwner.getId(), start.plusMinutes(40), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveBooking3, result.getContent().get(0));
        assertEquals(saveBooking2, result.getContent().get(1));
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStartGreaterThan(
                saveBooker.getId(), start.plusMinutes(80), pageable).getContent().size());
        assertEquals(0, bookingRepository.findAllByItemOwnerIdAndStartGreaterThan(
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
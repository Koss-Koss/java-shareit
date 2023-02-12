package ru.practicum.shareit.server.booking;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.server.booking.dto.*;
import ru.practicum.shareit.server.booking.model.*;
import ru.practicum.shareit.server.exception.InvalidConditionException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.ItemServiceImpl;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.item.dto.ItemIncomingDto;
import ru.practicum.shareit.server.pagination.PaginationUtils;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.SORT_START_DESC;

@Transactional
@SpringBootTest
class BookingServiceImplTestIT {
    private static final long UNKNOWN_ID = Long.MAX_VALUE;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private EntityManager em;

    UserDto owner = UserDto.builder().name("OwnerName").email("owner@test.com").build();
    UserDto booker = UserDto.builder().name("BookerName").email("booker@test.com").build();
    UserDto user = UserDto.builder().name("UserName").email("user@test.com").build();

    ItemIncomingDto itemIncomingDto = ItemIncomingDto.builder()
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .build();

    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = LocalDateTime.now().plusMinutes(55);

    String exceptionMessageNotUser = "Запрос на несуществующего пользователя с id = ";
    String exceptionMessageNotItem = "Запрос на несуществующую вещь с id = ";
    String exceptionMessageNotBooking = "Запрос на несуществующее бронирование с id = ";

    int from = 0;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC);

    @Test
    void create() {
        UserDto resultOwner = userService.create(owner);
        UserDto resultBooker = userService.create(booker);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        BookingIncomingDto bookingIncomingNotItemDto = BookingIncomingDto.builder()
                .itemId(UNKNOWN_ID)
                .start(start)
                .end(end)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.create(resultBooker.getId(), bookingIncomingNotItemDto));
        assertEquals(exceptionMessageNotItem + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> bookingService.create(UNKNOWN_ID, bookingIncomingNotItemDto));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start)
                .end(end)
                .build();

        exception = assertThrows(
                NotFoundException.class, () -> bookingService.create(resultOwner.getId(), bookingIncomingDto));
        assertEquals("Запрещено бронировать свои вещи: пользователь с id = " + resultOwner.getId() +
                " является владельцем вещи с id = " + resultItem.getId(), exception.getMessage());

        BookingDto resultBooking = bookingService.create(resultBooker.getId(), bookingIncomingDto);

        Booking savedBooking = em
                .createQuery("select b from Booking b where b.id = :id", Booking.class)
                .setParameter("id", resultBooking.getId())
                .getSingleResult();

        assertThat(savedBooking.getStart(), equalTo(resultBooking.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(resultBooking.getEnd()));
        assertThat(savedBooking.getItem().getId(), equalTo(resultBooking.getItem().getId()));
        assertThat(savedBooking.getBooker().getId(), equalTo(resultBooking.getBooker().getId()));
        assertThat(BookingStatus.WAITING, equalTo(resultBooking.getStatus()));

        ItemIncomingDto itemIncomingNotAvailableDto = itemIncomingDto.toBuilder().available(false).build();
        ItemDto resultItemNotAvailable = itemService.create(itemIncomingNotAvailableDto, resultOwner.getId());

        BookingIncomingDto bookingIncoming2Dto = BookingIncomingDto.builder()
                .itemId(resultItemNotAvailable.getId())
                .start(start)
                .end(end)
                .build();

        InvalidConditionException exception2 = assertThrows(
                InvalidConditionException.class, () ->
                        bookingService.create(resultBooker.getId(), bookingIncoming2Dto));
        assertEquals("Вещь с id = " + resultItemNotAvailable.getId() + " недоступна для бронирования",
                exception2.getMessage());
    }

    @Test
    void findByIdForUser() {
        UserDto resultOwner = userService.create(owner);
        UserDto resultBooker = userService.create(booker);
        UserDto resultUser = userService.create(user);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start)
                .end(end)
                .build();

        BookingDto resultBooking = bookingService.create(resultBooker.getId(), bookingIncomingDto);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> bookingService.findByIdForUser(UNKNOWN_ID, resultBooking.getId()));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> bookingService.findByIdForUser(resultBooker.getId(), UNKNOWN_ID));
        assertEquals(exceptionMessageNotBooking + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.findByIdForUser(resultUser.getId(), resultBooking.getId()));
        assertEquals("Невозможно получить бронирование id = " + resultBooking.getId() +
                " для данного пользователя", exception.getMessage());

        BookingDto savedBooking = bookingService.findByIdForUser(resultBooker.getId(), resultBooking.getId());

        assertThat(savedBooking.getStart(), equalTo(resultBooking.getStart()));
        assertThat(savedBooking.getEnd(), equalTo(resultBooking.getEnd()));
        assertThat(savedBooking.getItem().getId(), equalTo(resultBooking.getItem().getId()));
        assertThat(savedBooking.getBooker().getId(), equalTo(resultBooking.getBooker().getId()));
        assertThat(BookingStatus.WAITING, equalTo(resultBooking.getStatus()));
    }

    @Test
    void findAllWithStateForUser() {
        UserDto resultOwner = userService.create(owner);
        UserDto resultBooker = userService.create(booker);
        UserDto resultUser = userService.create(user);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        ItemIncomingDto itemIncoming2Dto = ItemIncomingDto.builder()
                .name("Item2Name")
                .description("Item2Description")
                .available(true)
                .build();
        ItemDto resultItem2 = itemService.create(itemIncoming2Dto, resultOwner.getId());

        BookingIncomingDto bookingPast1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusHours(4))
                .end(end.minusHours(4))
                .build();
        BookingIncomingDto bookingPast2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.minusHours(2))
                .end(end.minusHours(2))
                .build();
        BookingIncomingDto bookingCurrent1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusMinutes(20))
                .end(end.plusMinutes(20))
                .build();
        BookingIncomingDto bookingCurrent2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.minusMinutes(40))
                .end(end.plusMinutes(40))
                .build();
        BookingIncomingDto bookingFuture1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.plusHours(2))
                .end(end.plusHours(2))
                .build();
        BookingIncomingDto bookingFuture2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.plusHours(4))
                .end(end.plusHours(4))
                .build();
        BookingDto savedBookingPast1 = bookingService.create(resultBooker.getId(), bookingPast1);
        BookingDto savedBookingPast2 = bookingService.create(resultBooker.getId(), bookingPast2);
        BookingDto savedBookingCurrent1 = bookingService.create(resultBooker.getId(), bookingCurrent1);
        BookingDto savedBookingCurrent2 = bookingService.create(resultBooker.getId(), bookingCurrent2);
        BookingDto savedBookingFuture1 = bookingService.create(resultBooker.getId(), bookingFuture1);
        BookingDto savedBookingFuture2 = bookingService.create(resultBooker.getId(), bookingFuture2);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.findAllWithStateForUser(UNKNOWN_ID, BookingState.ALL, pageable));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        Page<BookingDto> resultAll =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.ALL, pageable);

        assertThat(6, equalTo(resultAll.getContent().size()));
        assertThat(resultAll.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1, savedBookingCurrent1,
                savedBookingCurrent2, savedBookingPast2, savedBookingPast1));

        Page<BookingDto> resultAllEmpty =
                bookingService.findAllWithStateForUser(resultUser.getId(), BookingState.ALL, pageable);

        assertThat(0, equalTo(resultAllEmpty.getContent().size()));

        Page<BookingDto> resultCurrent =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.CURRENT, pageable);

        assertThat(2, equalTo(resultCurrent.getContent().size()));
        assertThat(resultCurrent.getContent(), Matchers.containsInRelativeOrder(
                savedBookingCurrent1, savedBookingCurrent2));

        Page<BookingDto> resultPast =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.PAST, pageable);

        assertThat(2, equalTo(resultPast.getContent().size()));
        assertThat(resultPast.getContent(), Matchers.containsInRelativeOrder(
                savedBookingPast2, savedBookingPast1));

        Page<BookingDto> resultFuture =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.FUTURE, pageable);

        assertThat(2, equalTo(resultFuture.getContent().size()));
        assertThat(resultFuture.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1));

        BookingDto savedBookingPast1Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingPast1.getId(), false);
        BookingDto savedBookingPast2Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingPast2.getId(), false);
        BookingDto savedBookingCurrent1Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingCurrent1.getId(), false);

        Page<BookingDto> resultWaiting =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.WAITING, pageable);

        assertThat(3, equalTo(resultWaiting.getContent().size()));
        assertThat(resultWaiting.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1, savedBookingCurrent2));

        Page<BookingDto> resultRejected =
                bookingService.findAllWithStateForUser(resultBooker.getId(), BookingState.REJECTED, pageable);

        assertThat(3, equalTo(resultRejected.getContent().size()));
        assertThat(resultRejected.getContent(), Matchers.containsInRelativeOrder(
                savedBookingCurrent1Rejected, savedBookingPast2Rejected, savedBookingPast1Rejected));
    }

    @Test
    void findAllWithStateForOwner() {
        UserDto resultOwner = userService.create(owner);
        UserDto resultBooker = userService.create(booker);
        UserDto resultUser = userService.create(user);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        ItemIncomingDto itemIncoming2Dto = ItemIncomingDto.builder()
                .name("Item2Name")
                .description("Item2Description")
                .available(true)
                .build();
        ItemDto resultItem2 = itemService.create(itemIncoming2Dto, resultOwner.getId());

        BookingIncomingDto bookingPast1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusHours(4))
                .end(end.minusHours(4))
                .build();
        BookingIncomingDto bookingPast2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.minusHours(2))
                .end(end.minusHours(2))
                .build();
        BookingIncomingDto bookingCurrent1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusMinutes(20))
                .end(end.plusMinutes(20))
                .build();
        BookingIncomingDto bookingCurrent2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.minusMinutes(40))
                .end(end.plusMinutes(40))
                .build();
        BookingIncomingDto bookingFuture1 = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.plusHours(2))
                .end(end.plusHours(2))
                .build();
        BookingIncomingDto bookingFuture2 = BookingIncomingDto.builder()
                .itemId(resultItem2.getId())
                .start(start.plusHours(4))
                .end(end.plusHours(4))
                .build();
        BookingDto savedBookingPast1 = bookingService.create(resultBooker.getId(), bookingPast1);
        BookingDto savedBookingPast2 = bookingService.create(resultBooker.getId(), bookingPast2);
        BookingDto savedBookingCurrent1 = bookingService.create(resultBooker.getId(), bookingCurrent1);
        BookingDto savedBookingCurrent2 = bookingService.create(resultBooker.getId(), bookingCurrent2);
        BookingDto savedBookingFuture1 = bookingService.create(resultBooker.getId(), bookingFuture1);
        BookingDto savedBookingFuture2 = bookingService.create(resultBooker.getId(), bookingFuture2);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.findAllWithStateForOwner(UNKNOWN_ID, BookingState.ALL, pageable));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        Page<BookingDto> resultAll =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.ALL, pageable);

        assertThat(6, equalTo(resultAll.getContent().size()));
        assertThat(resultAll.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1, savedBookingCurrent1,
                savedBookingCurrent2, savedBookingPast2, savedBookingPast1));

        Page<BookingDto> resultAllEmpty =
                bookingService.findAllWithStateForOwner(resultUser.getId(), BookingState.ALL, pageable);

        assertThat(0, equalTo(resultAllEmpty.getContent().size()));

        Page<BookingDto> resultCurrent =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.CURRENT, pageable);

        assertThat(2, equalTo(resultCurrent.getContent().size()));
        assertThat(resultCurrent.getContent(), Matchers.containsInRelativeOrder(
                savedBookingCurrent1, savedBookingCurrent2));

        Page<BookingDto> resultPast =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.PAST, pageable);

        assertThat(2, equalTo(resultPast.getContent().size()));
        assertThat(resultPast.getContent(), Matchers.containsInRelativeOrder(
                savedBookingPast2, savedBookingPast1));

        Page<BookingDto> resultFuture =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.FUTURE, pageable);

        assertThat(2, equalTo(resultFuture.getContent().size()));
        assertThat(resultFuture.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1));

        BookingDto savedBookingPast1Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingPast1.getId(), false);
        BookingDto savedBookingPast2Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingPast2.getId(), false);
        BookingDto savedBookingCurrent1Rejected =
                bookingService.setApproved(resultOwner.getId(), savedBookingCurrent1.getId(), false);

        Page<BookingDto> resultWaiting =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.WAITING, pageable);

        assertThat(3, equalTo(resultWaiting.getContent().size()));
        assertThat(resultWaiting.getContent(), Matchers.containsInRelativeOrder(
                savedBookingFuture2, savedBookingFuture1, savedBookingCurrent2));

        Page<BookingDto> resultRejected =
                bookingService.findAllWithStateForOwner(resultOwner.getId(), BookingState.REJECTED, pageable);

        assertThat(3, equalTo(resultRejected.getContent().size()));
        assertThat(resultRejected.getContent(), Matchers.containsInRelativeOrder(
                savedBookingCurrent1Rejected, savedBookingPast2Rejected, savedBookingPast1Rejected));
    }

    @Test
    void setApproved() {
        UserDto resultOwner = userService.create(owner);
        UserDto resultBooker = userService.create(booker);
        UserDto resultUser = userService.create(user);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusHours(4))
                .end(end.minusHours(4))
                .build();
        BookingDto resultBooking = bookingService.create(resultBooker.getId(), bookingIncomingDto);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.setApproved(UNKNOWN_ID, resultBooking.getId(), true));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.setApproved(resultOwner.getId(), UNKNOWN_ID, true));
        assertEquals(exceptionMessageNotBooking + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () ->
                        bookingService.setApproved(resultUser.getId(), resultBooking.getId(), true));
        assertEquals("Запрещено одобрять бронирование чужих вещей: пользователь с id = " + resultUser.getId() +
                " не является владельцем вещи с id = " + resultBooking.getItem().getId() +
                " из бронирования с id = " + resultBooking.getId(), exception.getMessage());

        BookingDto resultApproved = bookingService.setApproved(resultOwner.getId(), resultBooking.getId(), true);

        BookingDto savedApproved = bookingService.findByIdForUser(resultBooker.getId(), resultApproved.getId());

        assertThat(BookingStatus.APPROVED, equalTo(savedApproved.getStatus()));
        assertThat(resultApproved, equalTo(savedApproved));

        InvalidConditionException exception2 = assertThrows(
                InvalidConditionException.class, () ->
                        bookingService.setApproved(resultOwner.getId(), resultBooking.getId(), true));
        assertEquals("Бронирование с id = " + resultBooking.getId() + " уже одобрено", exception2.getMessage());

        BookingIncomingDto bookingIncoming2Dto = BookingIncomingDto.builder()
                .itemId(resultItem.getId())
                .start(start.minusHours(4))
                .end(end.minusHours(4))
                .build();
        BookingDto resultBooking2 = bookingService.create(resultBooker.getId(), bookingIncoming2Dto);

        BookingDto resultRejected = bookingService.setApproved(resultOwner.getId(), resultBooking2.getId(), false);

        BookingDto savedRejected = bookingService.findByIdForUser(resultBooker.getId(), resultRejected.getId());

        assertThat(BookingStatus.REJECTED, equalTo(savedRejected.getStatus()));
        assertThat(resultRejected, equalTo(savedRejected));
    }
}

package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    long id = 1;

    long bookerId = 1;
    long ownerId = 2;
    long alienId = 2;
    User booker = User.builder().id(bookerId).name("Booker").email("booker@test.com").build();
    UserDto bookerDto = UserDto.builder().id(bookerId).name("Booker").email("booker@test.com").build();
    User owner = User.builder().id(ownerId).name("Owner").email("owner@test.com").build();
    User alien = User.builder().id(alienId).name("Alien").email("alien@test.com").build();

    long itemId = 1;
    Item item = Item.builder()
            .id(itemId)
            .name("TestItemName")
            .description("DescriptionItem")
            .available(true)
            .owner(owner)
            .build();
    ItemDto itemDto = ItemDto.builder()
            .id(itemId)
            .name("TestItemName")
            .description("DescriptionItem")
            .available(true)
            .build();
    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = LocalDateTime.now().plusMinutes(10);

    Booking booking = Booking.builder()
            .id(id)
            .start(start)
            .end(end)
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();

    BookingDto expectedBookingDto = BookingDto.builder()
            .id(id)
            .start(start)
            .end(end)
            .item(itemDto)
            .booker(bookerDto)
            .status(BookingStatus.WAITING)
            .build();

    BookingDto expectedBookingDtoWithStatusApproved = expectedBookingDto.toBuilder()
            .status(BookingStatus.APPROVED)
            .build();

    BookingDto expectedBookingDtoWithStatusRejected = expectedBookingDto.toBuilder()
            .status(BookingStatus.REJECTED)
            .build();

    BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
            .itemId(itemId)
            .start(start)
            .end(end)
            .build();

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    Page<Booking> pageBooking = new PageImpl<>(
            Collections.singletonList(booking), pageable, 1);
    Page<BookingDto> pageBookingDto = new PageImpl<>(
            Collections.singletonList(expectedBookingDto), pageable, 1);

    String exceptionMessage = "Message";

    @Test
    void findByIdForUser_whenBookingFoundAndValidAllParams_thenReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);

        assertEquals(expectedBookingDto, bookingService.findByIdForUser(bookerId, id));
        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).extract(anyLong());
    }

    @Test
    void findByIdForUser_whenUserNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.findByIdForUser(bookerId, id));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, never()).extract(anyLong());
    }

    @Test
    void findByIdForUser_whenBookingNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(alien);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.findByIdForUser(alienId, id));
        assertEquals("Невозможно получить бронирование id = " + id + " для данного пользователя",
                exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).extract(anyLong());
    }

    @Test
    void findByIdForUser_whenNotBookerAndNotOwner_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.findByIdForUser(bookerId, id));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).extract(anyLong());
    }

    @Test
    void findAllWithStateForUser_whenBookingStateALL_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.ALL, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenBookingStateWAITING_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.WAITING, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenBookingStateREJECTED_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.REJECTED, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenBookingStateCURRENT_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllCurrentForBooker(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.CURRENT, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllCurrentForBooker(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenBookingStatePAST_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdAndEndLessThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.PAST, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndLessThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenBookingStateFUTURE_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForUser(
                bookerId, BookingState.FUTURE, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartGreaterThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForUser_whenNotBooker_thenNotReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.findAllWithStateForUser(
                        bookerId, BookingState.ALL, pageable));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStateALL_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.ALL, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByItemOwnerIdOrderByStartDesc(anyLong(), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStateWAITING_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.WAITING, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStateREJECTED_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.REJECTED, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(
                anyLong(), any(BookingStatus.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStateCURRENT_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllCurrentForOwner(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.CURRENT, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllCurrentForOwner(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStatePAST_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.PAST, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndEndLessThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenBookingStateFUTURE_thenReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(pageBooking);

        assertEquals(pageBookingDto, bookingService.findAllWithStateForOwner(
                ownerId, BookingState.FUTURE, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartGreaterThanOrderByStartDesc(
                anyLong(), any(LocalDateTime.class), any(Pageable.class));
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void findAllWithStateForOwner_whenNotOwner_thenNotReturnedPageBookingDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.findAllWithStateForOwner(
                        ownerId, BookingState.ALL, pageable));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository, bookingRepository);
    }

    @Test
    void create_whenBookingCreated_thenReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenReturn(item);

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        assertEquals(expectedBookingDto, bookingService.create(bookerId, bookingIncomingDto));

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void create_whenUserNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.create(bookerId, bookingIncomingDto));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenItemNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.create(bookerId, bookingIncomingDto));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenBookerEqualsOwner_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(itemRepository.extract(anyLong())).thenReturn(item);

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.create(ownerId, bookingIncomingDto));
        assertEquals("Запрещено бронировать свои вещи: пользователь с id = " + ownerId +
                " является владельцем вещи с id = " + item.getId(), exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_whenItemAvailableIsFalse_thenNotReturnedBookingDto() {
        item.setAvailable(false);
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenReturn(item);

        InvalidConditionException exception =
                assertThrows(InvalidConditionException.class,
                        () -> bookingService.create(bookerId, bookingIncomingDto));
        assertEquals("Вещь с id = " + item.getId() + " недоступна для бронирования", exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproved_whenBookingSetStatusApproved_thenReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking.toBuilder().status(BookingStatus.APPROVED).build());

        assertEquals(expectedBookingDtoWithStatusApproved, bookingService.setApproved(ownerId, id, true));

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void setApproved_whenBookingSetStatusRejected_thenReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking.toBuilder().status(BookingStatus.REJECTED).build());

        assertEquals(expectedBookingDtoWithStatusRejected, bookingService.setApproved(ownerId, id, false));

        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void setApproved_whenUserNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.setApproved(bookerId, id, true));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproved_whenBookingNotFound_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.setApproved(bookerId, id, true));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproved_whenNotOwner_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> bookingService.setApproved(bookerId, id, true));
        assertEquals("Запрещено одобрять бронирование чужих вещей: пользователь с id = " + bookerId +
                " не является владельцем вещи с id = " + booking.getItem().getId() +
                " из бронирования с id = " + id, exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void setApproved_whenBookingAlreadyApproved_thenNotReturnedBookingDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.extract(anyLong())).thenReturn(booking);

        InvalidConditionException exception =
                assertThrows(InvalidConditionException.class, () -> bookingService.setApproved(ownerId, id, true));
        assertEquals("Бронирование с id = " + id + " уже одобрено", exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
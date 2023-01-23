package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    long ownerId = 1;
    long bookerId = 2;
    User owner = User.builder().id(ownerId).name("TestName").email("test@test.com").build();
    User booker = User.builder().id(bookerId).name("BookerName").email("booker@test.com").build();

    long itemId = 1;
    Item item = Item.builder()
            .id(itemId)
            .name("TestItemName")
            .description("ItemDescription")
            .available(true)
            .owner(owner)
            .build();

    Item oldItem = Item.builder()
            .id(itemId)
            .name("OldItemName")
            .description("OldItemDescription")
            .available(false)
            .owner(owner)
            .build();

    ItemIncomingDto itemIncomingDto = ItemIncomingDto.builder()
            .name(item.getName())
            .description(item.getDescription())
            .available(item.getAvailable())
            .requestId(null)
            .build();

    ItemDto expectedItemDto = ItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.getAvailable())
            .requestId(null)
            .build();

    long lastBookingId = 1;
    long nextBookingId = 2;
    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = LocalDateTime.now().plusMinutes(10);

    Booking lastBooking = Booking.builder()
            .id(lastBookingId)
            .start(start)
            .end(end)
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();
    Booking nextBooking = Booking.builder()
            .id(nextBookingId)
            .start(start)
            .end(end)
            .item(item)
            .booker(booker)
            .status(BookingStatus.WAITING)
            .build();
    BookingShortDto lastBookingShort = BookingShortDto.builder().id(lastBookingId).bookerId(booker.getId()).build();
    BookingShortDto nextBookingShort = BookingShortDto.builder().id(nextBookingId).bookerId(booker.getId()).build();

    long commentId = 1;
    String commentText = "CommentText";

    CommentIncomingDto commentIncomingDto = CommentIncomingDto.builder().text(commentText).build();
    Comment comment = Comment.builder()
            .id(commentId)
            .text(commentText)
            .item(item)
            .author(booker)
            .created(start)
            .build();
    CommentDto commentDto = CommentDto.builder()
            .id(commentId)
            .text(commentText)
            .authorName(booker.getName())
            .created(start)
            .build();
    Collection<Comment> comments =
            Collections.singletonList(Comment.builder().id(commentId).text("CommentText").build());
    Collection<CommentDto> commentsDto =
            Collections.singletonList(CommentDto.builder().id(commentId).text("CommentText").build());

    ItemDto expectedItemDtoForOwner = ItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.getAvailable())
            .lastBooking(lastBookingShort)
            .nextBooking(nextBookingShort)
            .comments(commentsDto)
            .build();

    ItemDto expectedItemDtoForNotOwner = ItemDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .available(item.getAvailable())
            .comments(commentsDto)
            .build();

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    Page<Item> pageItem = new PageImpl<>(
            Collections.singletonList(item), pageable, 1);
    Page<ItemDto> pageItemDto = new PageImpl<>(
            Collections.singletonList(expectedItemDto), pageable, 1);
    Page<ItemDto> pageBookingDtoForOwner = new PageImpl<>(
            Collections.singletonList(expectedItemDtoForOwner), pageable, 1);

    String exceptionMessage = "Message";

    @Test
    void findById_WhenItemFoundAndOwner_thenReturnedItemDto() {
        when(itemRepository.extract(anyLong())).thenReturn(item);
        when(bookingRepository.findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(lastBooking);
        when(bookingRepository.findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(nextBooking);
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(anyLong())).thenReturn(comments);

        assertEquals(expectedItemDtoForOwner, itemService.findById(ownerId, itemId));

        verify(itemRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1))
                .findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(1))
                .findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findAllByItem_IdOrderByCreatedDesc(anyLong());
    }

    @Test
    void findById_WhenItemFoundAndNotOwner_thenReturnedItemDto() {
        when(itemRepository.extract(anyLong())).thenReturn(item);
        when(bookingRepository.findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(lastBooking);
        when(bookingRepository.findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(nextBooking);
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(anyLong())).thenReturn(comments);

        assertEquals(expectedItemDtoForNotOwner, itemService.findById(bookerId, itemId));

        verify(itemRepository, times(1)).extract(anyLong());
        verify(bookingRepository, atMostOnce())
                .findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, atMostOnce())
                .findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findAllByItem_IdOrderByCreatedDesc(anyLong());
    }

    @Test
    void findById_whenItemNotFound_thenNotReturnedItemDto() {
        when(itemRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.findById(bookerId, itemId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(itemRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void findAllByOwnerId_whenOwnerFound_thenReturnedPageItemDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(bookingRepository.findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(lastBooking);
        when(bookingRepository.findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(nextBooking);
        when(commentRepository.findAllByItem_IdOrderByCreatedDesc(anyLong())).thenReturn(comments);
        when(itemRepository.findAllByOwnerId(anyLong(), any(Pageable.class))).thenReturn(pageItem);

        assertEquals(pageBookingDtoForOwner, itemService.findAllByOwnerId(
                ownerId, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1))
                .findByItemIdAndEndLessThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(bookingRepository, times(1))
                .findByItemIdAndStartGreaterThanOrderByStartDesc(anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).findAllByItem_IdOrderByCreatedDesc(anyLong());
        verify(itemRepository, times(1)).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    void findAllByOwnerId_whenOwnerNotFound_thenNotReturnedPageItemDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.findAllByOwnerId(bookerId, pageable));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void create_whenItemCreated_thenReturnedItemDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals(expectedItemDto, itemService.create(itemIncomingDto, ownerId));

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void create_whenUserNotFound_thenNotReturnedItemDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.findAllByOwnerId(bookerId, pageable));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenItemUpdated_thenUpdatedOnlyAvailableFields() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(itemRepository.extract(anyLong())).thenReturn(oldItem);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals(expectedItemDto, itemService.update(itemIncomingDto, itemId, ownerId));

        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item changedItem = itemArgumentCaptor.getValue();
        assertEquals(expectedItemDto.getDescription(),changedItem.getDescription());
        assertEquals(expectedItemDto.getAvailable(),changedItem.getAvailable());
        assertEquals(expectedItemDto.getName(),changedItem.getName());

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void update_whenUserNotFound_thenNotReturnedItemDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.update(itemIncomingDto, itemId, bookerId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void update_whenItemNotFound_thenNotReturnedItemDto() {
        when(itemRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.update(itemIncomingDto, itemId, bookerId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(itemRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void update_whenNotOwner_thenNotReturnedItemDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenReturn(item);

        ForbiddenException exception =
                assertThrows(ForbiddenException.class, () -> itemService.update(itemIncomingDto, itemId, bookerId));
        assertEquals("Не совпадают id пользователя из запроса и владельца вещи. " +
                "Только владелец может изменять/удалять вещь", exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void findAvailableByText_whenTextExists_thenReturnedPageItemDto() {
        when(itemRepository.findAvailableByText(anyString(), any(Pageable.class))).thenReturn(pageItem);

        assertEquals(pageItemDto, itemService.findAvailableByText(
                "teST", pageable));

        verify(itemRepository, times(1)).findAvailableByText(anyString(), any(Pageable.class));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void findAvailableByText_whenTextIsEmpty_thenReturnedPageItemDto() {

        assertEquals(new PageImpl<>(Collections.emptyList()), itemService.findAvailableByText(
                "", pageable));

        verify(itemRepository, never()).findAvailableByText(anyString(), any(Pageable.class));
    }

    @Test
    void createComment_whenCommentCreated_thenReturnedCommentDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenReturn(item);
        when(bookingRepository.countCompletedBookings(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(1);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        assertEquals(commentDto, itemService.createComment(bookerId, itemId, commentIncomingDto));

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).countCompletedBookings(anyLong(), anyLong(), any(LocalDateTime.class));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void createComment_whenUserNotFound_thenNotReturnedCommentDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.createComment(bookerId, itemId, commentIncomingDto));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createComment_whenItemNotFound_thenNotReturnedCommentDto() {
        when(userRepository.extract(anyLong())).thenReturn(booker);
        when(itemRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemService.createComment(bookerId, itemId, commentIncomingDto));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository, itemRepository);
    }

    @Test
    void createComment_whenAuthorNotBooker_thenNotReturnedCommentDto() {
        when(userRepository.extract(anyLong())).thenReturn(owner);
        when(itemRepository.extract(anyLong())).thenReturn(item);
        when(bookingRepository.countCompletedBookings(anyLong(), anyLong(), any(LocalDateTime.class))).thenReturn(0);

        InvalidConditionException exception =
                assertThrows(InvalidConditionException.class, () -> itemService.createComment(ownerId, itemId, commentIncomingDto));
        assertEquals("Запрещены комментарии пользователей, не арендовавших вещь", exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).extract(anyLong());
        verify(bookingRepository, times(1)).countCompletedBookings(anyLong(), anyLong(), any(LocalDateTime.class));
        verifyNoMoreInteractions(userRepository, itemRepository, bookingRepository);
    }






}
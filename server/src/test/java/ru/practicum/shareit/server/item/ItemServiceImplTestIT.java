package ru.practicum.shareit.server.item;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.server.booking.BookingServiceImpl;
import ru.practicum.shareit.server.booking.dto.BookingDto;
import ru.practicum.shareit.server.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.item.model.*;
import ru.practicum.shareit.server.pagination.PaginationUtils;
import ru.practicum.shareit.server.request.ItemRequestServiceImpl;
import ru.practicum.shareit.server.request.dto.ItemRequestIncomingDto;
import ru.practicum.shareit.server.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.server.user.UserServiceImpl;
import ru.practicum.shareit.server.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@Transactional
@SpringBootTest
class ItemServiceImplTestIT {
    private static final long UNKNOWN_ID = Long.MAX_VALUE;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRequestServiceImpl requestService;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private EntityManager em;

    UserDto owner = UserDto.builder().name("OwnerName").email("owner@test.com").build();
    UserDto booker = UserDto.builder().name("BookerName").email("booker@test.com").build();
    UserDto requester = UserDto.builder().name("RequesterName").email("requester@test.com").build();

    ItemIncomingDto itemIncomingDto = ItemIncomingDto.builder()
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .build();

    ItemIncomingDto itemIncomingWithBookingDto = ItemIncomingDto.builder()
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .build();

    ItemRequestIncomingDto requestIncomingDto =
            ItemRequestIncomingDto.builder().description("RequestDescription").build();

    CommentIncomingDto commentIncomingDto = CommentIncomingDto.builder().text("CommentText").build();

    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = LocalDateTime.now().plusMinutes(55);

    String exceptionMessageNotUser = "Запрос на несуществующего пользователя с id = ";
    String exceptionMessageNotItem = "Запрос на несуществующую вещь с id = ";
    String exceptionMessageNotItemRequest = "Запрос на несуществующий для поиска нужной вещи запрос с id = ";

    int from = 0;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);

    @Test
    void create() {
        UserDto resultOwner = userService.create(owner);
        ItemDto resultItem = itemService.create(itemIncomingDto, resultOwner.getId());

        Item savedItem = em
                .createQuery(
                        "select i from Item i where i.name = :name",
                        Item.class)
                .setParameter("name", itemIncomingDto.getName())
                .getSingleResult();

        assertThat(savedItem.getId(), equalTo(resultItem.getId()));
        assertThat(savedItem.getName(), equalTo(resultItem.getName()));
        assertThat(savedItem.getDescription(), equalTo(resultItem.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(resultItem.getAvailable()));
        assertThat(savedItem.getRequestId(), equalTo(resultItem.getRequestId()));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.create(itemIncomingDto, UNKNOWN_ID));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        UserDto resultRequester = userService.create(requester);
        ItemRequestShortDto resultRequest = requestService.create(requestIncomingDto, resultRequester.getId());
        ItemIncomingDto itemIncomingDto2 = itemIncomingDto.toBuilder().requestId(resultRequest.getId()).build();
        ItemDto resultItemWithRequestId = itemService.create(itemIncomingDto2, resultOwner.getId());

        Item savedItemWithRequestId = em
                .createQuery(
                        "select i from Item i where i.name = :name and i.requestId = :requestId",
                        Item.class)
                .setParameter("name", itemIncomingDto2.getName())
                .setParameter("requestId", itemIncomingDto2.getRequestId())
                .getSingleResult();

        assertThat(savedItemWithRequestId.getId(), equalTo(resultItemWithRequestId.getId()));
        assertThat(savedItemWithRequestId.getName(), equalTo(resultItemWithRequestId.getName()));
        assertThat(savedItemWithRequestId.getDescription(), equalTo(resultItemWithRequestId.getDescription()));
        assertThat(savedItemWithRequestId.getAvailable(), equalTo(resultItemWithRequestId.getAvailable()));
        assertThat(savedItemWithRequestId.getRequestId(), equalTo(resultItemWithRequestId.getRequestId()));

        ItemIncomingDto itemIncomingDto3 = itemIncomingDto.toBuilder().requestId(UNKNOWN_ID).build();
        exception = assertThrows(
                NotFoundException.class, () -> itemService.create(itemIncomingDto3, resultOwner.getId()));
        assertEquals(exceptionMessageNotItemRequest + UNKNOWN_ID, exception.getMessage());
    }

    @Test
    void findById() {
        UserDto savedOwner = userService.create(owner);
        UserDto savedBooker = userService.create(booker);
        UserDto savedRequester = userService.create(requester);
        ItemDto savedItem = itemService.create(itemIncomingDto, savedOwner.getId());

        BookingIncomingDto lastBookingIncomingDto = BookingIncomingDto.builder()
                .itemId(savedItem.getId())
                .start(start.minusHours(2))
                .end(end.minusHours(2))
                .build();

        BookingIncomingDto nextBookingIncomingDto = BookingIncomingDto.builder()
                .itemId(savedItem.getId())
                .start(start.plusHours(2))
                .end(end.plusHours(2))
                .build();

        BookingDto savedLastBookingWaiting = bookingService.create(savedBooker.getId(), lastBookingIncomingDto);
        BookingDto savedLastBooking =
                bookingService.setApproved(savedOwner.getId(), savedLastBookingWaiting.getId(), true);
        BookingDto savedNextBooking = bookingService.create(savedBooker.getId(), nextBookingIncomingDto);

        CommentDto savedComment = itemService.createComment(savedBooker.getId(), savedItem.getId(), commentIncomingDto);

        ItemDto resultItem = itemService.findById(savedOwner.getId(), savedItem.getId());

        assertThat(savedItem.getId(), equalTo(resultItem.getId()));
        assertThat(savedItem.getName(), equalTo(resultItem.getName()));
        assertThat(savedItem.getDescription(), equalTo(resultItem.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(resultItem.getAvailable()));
        assertThat(savedLastBooking.getId(), equalTo(resultItem.getLastBooking().getId()));
        assertThat(savedLastBooking.getBooker().getId(), equalTo(resultItem.getLastBooking().getBookerId()));
        assertThat(savedNextBooking.getId(), equalTo(resultItem.getNextBooking().getId()));
        assertThat(savedNextBooking.getBooker().getId(), equalTo(resultItem.getNextBooking().getBookerId()));
        assertThat(1, equalTo(resultItem.getComments().size()));
        assertThat(savedComment.getId(), equalTo(resultItem.getComments().iterator().next().getId()));
        assertThat(savedComment.getAuthorName(), equalTo(resultItem.getComments().iterator().next().getAuthorName()));
        assertThat(savedComment.getText(), equalTo(resultItem.getComments().iterator().next().getText()));


        resultItem = itemService.findById(savedRequester.getId(), savedItem.getId());

        assertThat(savedItem.getId(), equalTo(resultItem.getId()));
        assertThat(savedItem.getName(), equalTo(resultItem.getName()));
        assertThat(savedItem.getDescription(), equalTo(resultItem.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(resultItem.getAvailable()));
        assertThat(null, equalTo(resultItem.getLastBooking()));
        assertThat(null, equalTo(resultItem.getNextBooking()));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.findById(savedOwner.getId(), UNKNOWN_ID));
        assertEquals(exceptionMessageNotItem + UNKNOWN_ID, exception.getMessage());
    }

    @Test
    void findAllByOwnerId() {
        UserDto savedOwner = userService.create(owner);
        UserDto savedBooker = userService.create(booker);
        UserDto savedRequester = userService.create(requester);
        itemService.create(itemIncomingDto, savedBooker.getId());
        ItemDto savedItem = itemService.create(itemIncomingWithBookingDto, savedOwner.getId());

        BookingIncomingDto lastBookingIncomingDto = BookingIncomingDto.builder()
                .itemId(savedItem.getId())
                .start(start.minusHours(2))
                .end(end.minusHours(2))
                .build();

        BookingIncomingDto nextBookingIncomingDto = BookingIncomingDto.builder()
                .itemId(savedItem.getId())
                .start(start.plusHours(2))
                .end(end.plusHours(2))
                .build();

        BookingDto savedLastBookingWaiting = bookingService.create(savedBooker.getId(), lastBookingIncomingDto);
        BookingDto savedLastBooking =
                bookingService.setApproved(savedOwner.getId(), savedLastBookingWaiting.getId(), true);
        BookingDto savedNextBooking = bookingService.create(savedRequester.getId(), nextBookingIncomingDto);

        CommentDto savedComment = itemService.createComment(savedBooker.getId(), savedItem.getId(), commentIncomingDto);

        Page<ItemDto> result = itemService.findAllByOwnerId(savedOwner.getId(), pageable);

        assertThat(1, equalTo(result.getContent().size()));
        assertThat(savedItem.getId(), equalTo(result.getContent().get(0).getId()));
        assertThat(savedItem.getName(), equalTo(result.getContent().get(0).getName()));
        assertThat(savedItem.getDescription(), equalTo(result.getContent().get(0).getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(result.getContent().get(0).getAvailable()));
        assertThat(savedLastBooking.getId(), equalTo(result.getContent().get(0).getLastBooking().getId()));
        assertThat(savedLastBooking.getBooker().getId(), equalTo(result.getContent().get(0).getLastBooking().getBookerId()));
        assertThat(savedNextBooking.getId(), equalTo(result.getContent().get(0).getNextBooking().getId()));
        assertThat(savedNextBooking.getBooker().getId(), equalTo(result.getContent().get(0).getNextBooking().getBookerId()));
        assertThat(1, equalTo(result.getContent().get(0).getComments().size()));
        assertThat(savedComment.getId(), equalTo(result.getContent().get(0).getComments().iterator().next().getId()));
        assertThat(savedComment.getAuthorName(), equalTo(result.getContent().get(0).getComments().iterator().next().getAuthorName()));
        assertThat(savedComment.getText(), equalTo(result.getContent().get(0).getComments().iterator().next().getText()));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.findAllByOwnerId(UNKNOWN_ID, pageable));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());
    }

    @Test
    void update() {
        UserDto savedOwner = userService.create(owner);
        UserDto savedRequester = userService.create(requester);
        ItemRequestShortDto saveRequest = requestService.create(requestIncomingDto, savedRequester.getId());

        ItemDto savedItem = itemService.create(itemIncomingDto, savedOwner.getId());

        ItemIncomingDto itemIncomingUpdateWithOutRequestIdDto = ItemIncomingDto.builder()
                .name("NewName")
                .description("NewDescription")
                .requestId(UNKNOWN_ID)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.update(
                        itemIncomingUpdateWithOutRequestIdDto, savedItem.getId(), UNKNOWN_ID));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> itemService.update(
                        itemIncomingUpdateWithOutRequestIdDto, UNKNOWN_ID, savedOwner.getId()));
        assertEquals(exceptionMessageNotItem + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> itemService.update(
                        itemIncomingUpdateWithOutRequestIdDto, savedItem.getId(), savedOwner.getId()));
        assertEquals(exceptionMessageNotItemRequest + UNKNOWN_ID, exception.getMessage());

        ItemIncomingDto itemIncomingUpdateDto = itemIncomingUpdateWithOutRequestIdDto.toBuilder()
                .requestId(saveRequest.getId())
                .build();

        ForbiddenException exception2 = assertThrows(
                ForbiddenException.class, () -> itemService.update(
                        itemIncomingUpdateDto, savedItem.getId(), savedRequester.getId()));
        assertEquals("Не совпадают id пользователя из запроса и владельца вещи. " +
                "Только владелец может изменять/удалять вещь", exception2.getMessage());

        ItemDto result = itemService.update(itemIncomingUpdateDto, savedItem.getId(), savedOwner.getId());

        Item updatedItem = em
                .createQuery(
                        "select i from Item i where i.id = :id",
                        Item.class)
                .setParameter("id", savedItem.getId())
                .getSingleResult();

        assertThat(itemIncomingUpdateDto.getName(), equalTo(updatedItem.getName()));
        assertThat(itemIncomingUpdateDto.getDescription(), equalTo(updatedItem.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(updatedItem.getAvailable()));

        assertThat(savedItem.getId(), equalTo(result.getId()));
        assertThat(itemIncomingUpdateDto.getName(), equalTo(result.getName()));
        assertThat(itemIncomingUpdateDto.getDescription(), equalTo(result.getDescription()));
        assertThat(savedItem.getAvailable(), equalTo(result.getAvailable()));

        ItemIncomingDto itemIncomingUpdate2Dto = ItemIncomingDto.builder()
                .available(false)
                .build();

        result = itemService.update(itemIncomingUpdate2Dto, savedItem.getId(), savedOwner.getId());

        Item updatedItem2 = em
                .createQuery(
                        "select i from Item i where i.id = :id",
                        Item.class)
                .setParameter("id", savedItem.getId())
                .getSingleResult();

        assertThat(updatedItem.getName(), equalTo(updatedItem2.getName()));
        assertThat(updatedItem.getDescription(), equalTo(updatedItem2.getDescription()));
        assertThat(itemIncomingUpdate2Dto.getAvailable(), equalTo(updatedItem2.getAvailable()));

        assertThat(savedItem.getId(), equalTo(result.getId()));
        assertThat(updatedItem.getName(), equalTo(result.getName()));
        assertThat(updatedItem.getDescription(), equalTo(result.getDescription()));
        assertThat(itemIncomingUpdate2Dto.getAvailable(), equalTo(result.getAvailable()));
    }

    @Test
    void findAvailableByText() {
        UserDto savedOwner = userService.create(owner);

        ItemIncomingDto itemIncoming2Dto = ItemIncomingDto.builder()
                .name("ItemDescriptionName")
                .description("ItemNameDescription")
                .available(true)
                .build();

        ItemIncomingDto itemIncoming3Dto = ItemIncomingDto.builder()
                .name("NeutralName")
                .description("NeutralDescription")
                .available(true)
                .build();

        ItemDto savedItem = itemService.create(itemIncomingDto, savedOwner.getId());
        ItemDto savedItem2 = itemService.create(itemIncoming2Dto, savedOwner.getId());
        itemService.create(itemIncoming3Dto, savedOwner.getId());

        Page<ItemDto> result = itemService.findAvailableByText("EmNaM", pageable);

        assertThat(2, equalTo(result.getContent().size()));
        assertThat(result, Matchers.containsInAnyOrder(savedItem, savedItem2));

        result = itemService.findAvailableByText("", pageable);
        assertThat(0, equalTo(result.getContent().size()));
    }

    @Test
    void createComment() {
        UserDto savedOwner = userService.create(owner);
        UserDto savedBooker = userService.create(booker);
        ItemDto savedItem = itemService.create(itemIncomingDto, savedOwner.getId());

        BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
                .itemId(savedItem.getId())
                .start(start.minusHours(2))
                .end(end.minusHours(2))
                .build();
        BookingDto savedBookingWaiting = bookingService.create(savedBooker.getId(), bookingIncomingDto);
        bookingService.setApproved(savedOwner.getId(), savedBookingWaiting.getId(), true);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> itemService.createComment(
                        UNKNOWN_ID, savedItem.getId(), commentIncomingDto));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> itemService.createComment(
                        savedBooker.getId(), UNKNOWN_ID, commentIncomingDto));
        assertEquals(exceptionMessageNotItem + UNKNOWN_ID, exception.getMessage());

        InvalidConditionException exception2 = assertThrows(
                InvalidConditionException.class, () -> itemService.createComment(
                        savedOwner.getId(), savedItem.getId(), commentIncomingDto));
        assertEquals("Запрещены комментарии пользователей, не арендовавших вещь", exception2.getMessage());

        CommentDto result = itemService.createComment(
                savedBooker.getId(), savedItem.getId(), commentIncomingDto);

        Comment savedComment = em
                .createQuery(
                        "select c from Comment c where c.text = :text",
                        Comment.class)
                .setParameter("text", commentIncomingDto.getText())
                .getSingleResult();

        assertThat(savedComment.getId(), equalTo(result.getId()));
        assertThat(savedComment.getText(), equalTo(result.getText()));
        assertThat(savedComment.getAuthor().getName(), equalTo(result.getAuthorName()));
    }

}

package ru.practicum.shareit.server.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.server.booking.dto.*;
import ru.practicum.shareit.server.booking.model.BookingStatus;
import ru.practicum.shareit.server.exception.InvalidConditionException;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.dto.ItemDto;
import ru.practicum.shareit.server.pagination.PaginationUtils;
import ru.practicum.shareit.server.user.dto.UserDto;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.server.ShareItServerConstants.COMMON_BOOKING_PATH;
import static ru.practicum.shareit.server.booking.BookingController.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.SORT_START_DESC;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private BookingController bookingController;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    long id = 1;
    long userId = 1;
    long itemId = 1;
    LocalDateTime start = LocalDateTime.now().plusMinutes(5);
    LocalDateTime end = LocalDateTime.now().plusMinutes(10);

    BookingDto expectedBookingDto = BookingDto.builder()
            .id(id)
            .start(start)
            .end(end)
            .item(new ItemDto())
            .booker(UserDto.builder().name("name").build())
            .status(BookingStatus.WAITING)
            .build();

    BookingIncomingDto bookingIncomingDto = BookingIncomingDto.builder()
            .itemId(itemId)
            .start(start)
            .end(end)
            .build();

    int from = 0;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_START_DESC);
    Page<BookingDto> pageBookingDto = new PageImpl<>(
            Collections.singletonList(expectedBookingDto), pageable, 1);

    @Test
    @DisplayName("Метод getByIdForUser - Успех: запрос от арендатора или владельца")
    void getByIdForUser_whenValidAllParamsAndBookerOrOwner_thenResponseStatusOkWithBookingDtoInBody() throws Exception {
        when(bookingService.findByIdForUser(anyLong(), anyLong()))
                .thenReturn(expectedBookingDto);

        mvc.perform(get(COMMON_BOOKING_PATH + BOOKING_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedBookingDto)));
        verify(bookingService, times(1)).findByIdForUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getByIdForUser - Плохой id, userId и запрос не от арендатора или владельца")
    void getByIdForUser_whenInvalidParamsOrNotBookerOrNotOwner_thenResponseStatusNotFound() throws Exception {
        when(bookingService.findByIdForUser(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + BOOKING_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).findByIdForUser(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getAllWithStateForUser - Успех")
    void getAllWithStateForUser_whenValidAllParams_thenResponseStatusOkWithBookingDtoCollectionInBody()
            throws Exception {
        when(bookingService.findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(pageBookingDto);

        mvc.perform(get(COMMON_BOOKING_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(pageBookingDto.getContent())));
        verify(bookingService, times(1))
                .findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllWithStateForUser - Плохой userId")
    void getAllWithStateForUser_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        when(bookingService.findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1))
                .findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllWithStateForUser - Плохой state, from или size")
    void getAllWithStateForUser_whenInvalidStateOrFromOrSize_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(bookingService, times(1))
                .findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllWithStateForOwner - Успех")
    void findAllWithStateForOwner_whenValidAllParams_thenResponseStatusOkWithBookingDtoCollectionInBody()
            throws Exception {
        when(bookingService.findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenReturn(pageBookingDto);

        mvc.perform(get(COMMON_BOOKING_PATH + OWNER_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(pageBookingDto.getContent())));
        verify(bookingService, times(1))
                .findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllWithStateForOwner - Плохой userId")
    void findAllWithStateForOwner_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        when(bookingService.findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + OWNER_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1))
                .findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllWithStateForOwner - Плохой state, from или size")
    void findAllWithStateForOwner_whenInvalidStateOrFromOrSize_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + OWNER_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(bookingService, times(1))
                .findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод create - Успех")
    void create_whenValidAllParams_thenResponseStatusOkWithBookingDtoInBody() throws Exception {
        when(bookingService.create(anyLong(), any(BookingIncomingDto.class)))
                .thenReturn(expectedBookingDto);

        mvc.perform(post(COMMON_BOOKING_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedBookingDto)));
        verify(bookingService, times(1)).create(anyLong(), any(BookingIncomingDto.class));
    }

    @Test
    @DisplayName("Метод create - Плохой userId или itemId")
    void create_whenInvalidUserIdOrItemId_thenResponseStatusNotFound() throws Exception {
        when(bookingService.create(anyLong(), any(BookingIncomingDto.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(post(COMMON_BOOKING_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).create(anyLong(), any(BookingIncomingDto.class));
    }

    @Test
    @DisplayName("Метод create - Плохие входные данные")
    void create_whenInvalidBookingIncomingDto_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.create(anyLong(), any(BookingIncomingDto.class)))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(post(COMMON_BOOKING_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(bookingIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(bookingService, times(1)).create(anyLong(), any(BookingIncomingDto.class));
    }

    @Test
    @DisplayName("Метод update - Успех: запрос от владельца")
    void setApproved_whenValidAllParamsAndOwner_thenResponseStatusOkWithBookingDtoInBody() throws Exception {
        when(bookingService.setApproved(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(expectedBookingDto);

        mvc.perform(patch(COMMON_BOOKING_PATH + BOOKING_PREFIX + APPROVED_PREFIX + "true", id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedBookingDto)));
        verify(bookingService, times(1)).setApproved(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Метод update - Плохой userId или bookingId")
    void setApproved_whenInvalidUserIdOrBookingId_thenResponseStatusOkWithBookingDtoInBody() throws Exception {
        when(bookingService.setApproved(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(NotFoundException.class);

        mvc.perform(patch(COMMON_BOOKING_PATH + BOOKING_PREFIX + APPROVED_PREFIX + "true", id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService, times(1)).setApproved(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Метод update - Когда статус уже approved")
    void setApproved_whenStatusAlreadyApproved_thenResponseStatusOkWithBookingDtoInBody() throws Exception {
        when(bookingService.setApproved(anyLong(), anyLong(), anyBoolean()))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(patch(COMMON_BOOKING_PATH + BOOKING_PREFIX + APPROVED_PREFIX + "true", id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(bookingService, times(1)).setApproved(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Приватный метод parseBookingState - Успех: хороший state")
    void parseBookingState_whenValidState_thenReturnedBookingState() {

        assertEquals(bookingController.parseBookingState("ALL"), BookingState.ALL);
    }

    @Test
    @DisplayName("Приватный метод parseBookingState - Плохой state")
    void parseBookingState_whenInvalidState_thenNotParsedState() {

        InvalidConditionException exception =
                assertThrows(InvalidConditionException.class, () -> bookingController.parseBookingState("---"));
        assertEquals("Unknown state: ---", exception.getMessage());
    }
}

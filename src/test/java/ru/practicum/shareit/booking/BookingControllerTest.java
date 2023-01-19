package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingIncomingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.ShareItAppConstants.COMMON_BOOKING_PATH;
import static ru.practicum.shareit.booking.BookingController.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

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

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    Page<BookingDto> pageBookingDto = new PageImpl<>(
            Collections.singletonList(expectedBookingDto), pageable, 1);

    @Test
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
    void getAllWithStateForUser_whenInvalidStateOrFromOrSize_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(bookingService, times(1))
                .findAllWithStateForUser(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
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
    void findAllWithStateForOwner_whenInvalidStateOrFromOrSize_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class)))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(get(COMMON_BOOKING_PATH + OWNER_PATH + STATE_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(bookingService, times(1))
                .findAllWithStateForOwner(anyLong(), any(BookingState.class), any(Pageable.class));
    }

    @Test
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
    void create_whenInvalidBookingIncomingDto_thenResponseStatusBadRequest() throws Exception {
        when(bookingService.create(anyLong(), any(BookingIncomingDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST));

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
}
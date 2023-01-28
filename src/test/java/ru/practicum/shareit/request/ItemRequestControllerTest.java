package ru.practicum.shareit.request;

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
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.request.dto.*;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.ShareItAppConstants.COMMON_ITEM_REQUEST_PATH;
import static ru.practicum.shareit.pagination.PaginationConstant.SORT_CREATED_DESC;
import static ru.practicum.shareit.request.ItemRequestController.ALL_PATH;
import static ru.practicum.shareit.request.ItemRequestController.ITEM_REQUEST_PREFIX;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    long id = 1;
    long userId = 1;
    String description = "ItemRequestDescription";
    LocalDateTime created = LocalDateTime.now().plusMinutes(5);

    ItemRequestDto expectedItemRequestDto = ItemRequestDto.builder()
            .id(id)
            .description(description)
            .created(created)
            .items(Collections.emptyList())
            .build();

    ItemRequestShortDto expectedItemRequestShortDto = ItemRequestShortDto.builder()
            .id(id)
            .description(description)
            .created(created)
            .build();

    ItemRequestIncomingDto itemRequestIncomingDto = ItemRequestIncomingDto.builder()
            .description(description)
            .build();

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_CREATED_DESC);
    Page<ItemRequestDto> pageItemRequestDto = new PageImpl<>(
            Collections.singletonList(expectedItemRequestDto), pageable, 1);

    @Test
    @DisplayName("Метод getItemRequestById - Успех")
    void getItemRequestById_whenValidAllParams_thenResponseStatusOkWithItemRequestDtoInBody() throws Exception {
        when(itemRequestService.findById(anyLong(), anyLong()))
                .thenReturn(expectedItemRequestDto);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH + ITEM_REQUEST_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemRequestDto)));
        verify(itemRequestService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getItemRequestById - Плохой userId или requestId")
    void getItemRequestById_whenInvalidUserIdOrRequestId_thenResponseStatusNotFound() throws Exception {
        when(itemRequestService.findById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH + ITEM_REQUEST_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getAllByRequesterId - Успех")
    void getAllByRequesterId_whenValidRequesterId_thenResponseStatusOkWithItemRequestDtoCollectionInBody()
            throws Exception {
        when(itemRequestService.findAllByRequesterId(anyLong()))
                .thenReturn(Collections.singletonList(expectedItemRequestDto));

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        mapper.writeValueAsString(Collections.singletonList(expectedItemRequestDto))));
        verify(itemRequestService, times(1)).findAllByRequesterId(anyLong());
    }

    @Test
    @DisplayName("Метод getAllByRequesterId - Плохой requesterId")
    void getAllByRequesterId_whenInvalidRequesterId_thenResponseStatusNotFound() throws Exception {
        when(itemRequestService.findAllByRequesterId(anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(1)).findAllByRequesterId(anyLong());
    }

    @Test
    @DisplayName("Метод getAllByExpectRequesterId - Успех")
    void getAllByExpectRequesterId_whenValidAllParams_thenResponseStatusOkWithBookingDtoCollectionInBody()
            throws Exception {
        when(itemRequestService.findAllByExpectRequesterId(anyLong(), any(Pageable.class)))
                .thenReturn(pageItemRequestDto);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH + ALL_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(pageItemRequestDto.getContent())));
        verify(itemRequestService, times(1))
                .findAllByExpectRequesterId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllByExpectRequesterId - Плохой requesterId")
    void getAllByExpectRequesterId_whenInvalidRequesterId_thenResponseStatusNotFound() throws Exception {
        when(itemRequestService.findAllByExpectRequesterId(anyLong(), any(Pageable.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH + ALL_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(1))
                .findAllByExpectRequesterId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllByExpectRequesterId - Плохой from или size")
    void getAllByExpectRequesterId_whenInvalidFromOrSize_thenResponseStatusBadRequest() throws Exception {
        when(itemRequestService.findAllByExpectRequesterId(anyLong(), any(Pageable.class)))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(get(COMMON_ITEM_REQUEST_PATH + ALL_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemRequestService, times(1))
                .findAllByExpectRequesterId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод create - Успех")
    void create_whenValidAllParams_thenResponseStatusOkWithItemRequestShortDtoInBody() throws Exception {
        when(itemRequestService.create(any(ItemRequestIncomingDto.class), anyLong()))
                .thenReturn(expectedItemRequestShortDto);

        mvc.perform(post(COMMON_ITEM_REQUEST_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemRequestShortDto)));
        verify(itemRequestService, times(1)).create(any(ItemRequestIncomingDto.class), anyLong());
    }

    @Test
    @DisplayName("Метод create - Плохой userId")
    void create_whenInvalidUserId_thenResponseStatusNotFound() throws Exception {
        when(itemRequestService.create(any(ItemRequestIncomingDto.class), anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(post(COMMON_ITEM_REQUEST_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemRequestService, times(1)).create(any(ItemRequestIncomingDto.class), anyLong());
    }

    @Test
    @DisplayName("Метод create - Плохие входные данные")
    void create_whenInvalidItemRequestIncomingDto_thenResponseStatusBadRequest() throws Exception {
        when(itemRequestService.create(any(ItemRequestIncomingDto.class), anyLong()))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(post(COMMON_ITEM_REQUEST_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(itemRequestService, times(1)).create(any(ItemRequestIncomingDto.class), anyLong());
    }
}
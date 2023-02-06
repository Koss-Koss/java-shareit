package ru.practicum.shareit.server.item;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.server.booking.dto.BookingShortDto;
import ru.practicum.shareit.server.exception.*;
import ru.practicum.shareit.server.item.dto.*;
import ru.practicum.shareit.server.pagination.PaginationUtils;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.server.ShareItServerConstants.COMMON_ITEM_PATH;
import static ru.practicum.shareit.server.item.ItemController.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    long id = 1;
    long userId = 1;
    long requestId = 1;
    ItemDto itemDto = ItemDto.builder()
            .name("TestItemName")
            .description("DescriptionItem")
            .available(true)
            .build();
    ItemDto expectedItemDtoForUser = itemDto.toBuilder()
            .id(id)
            .comments(Collections.emptyList())
            .build();

    ItemDto expectedItemDto = itemDto.toBuilder()
            .id(id)
            .build();

    ItemDto expectedItemDtoForOwner = itemDto.toBuilder()
            .id(id)
            .lastBooking(BookingShortDto.builder().build())
            .nextBooking(BookingShortDto.builder().build())
            .comments(Collections.emptyList())
            .build();

    ItemIncomingDto itemIncomingDto = ItemIncomingDto.builder()
            .name("TestItemName")
            .description("DescriptionItem")
            .available(true)
            .requestId(requestId)
            .build();
    ItemDto expectedItemDtoWithRequestId = itemDto.toBuilder()
            .id(id)
            .requestId(requestId)
            .build();

    String textComment = "textComment";
    CommentIncomingDto commentIncomingDto = CommentIncomingDto.builder()
            .text(textComment)
            .build();
    CommentDto expectedCommentDto = CommentDto.builder()
            .id(1L)
            .text(textComment)
            .authorName("authorName")
            .created(LocalDateTime.now())
            .build();

    int from = 0;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    Page<ItemDto> pageItemDtoForOwner = new PageImpl<>(
            Collections.singletonList(expectedItemDtoForOwner), pageable, 1);

    Page<ItemDto> pageItemDtoForSearch = new PageImpl<>(
            Collections.singletonList(expectedItemDto), pageable, 1);

    @Test
    @DisplayName("Метод getItemById - Успех: запрос от не владельца")
    void getItemById_whenValidItemIdAndUser_thenResponseStatusOkWithItemDtoForUserInBody() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(expectedItemDtoForUser);

        mvc.perform(get(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemDtoForUser)));
        verify(itemService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getItemById - Успех: запрос от владельца")
    void getItemById_whenValidItemIdAndOwner_thenResponseStatusOkWithItemDtoForOwnerInBody() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenReturn(expectedItemDtoForOwner);

        mvc.perform(get(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemDtoForOwner)));
        verify(itemService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getItemById - Плохой userId или itemId")
    void getItemById_whenInvalidUserIdOrItemId_thenResponseStatusNotFound() throws Exception {
        when(itemService.findById(anyLong(), anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).findById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getAllByOwnerId - Успех: запрос от владельца")
    void getAllByOwnerId_whenValidAllParamsAndOwner_thenResponseStatusOkWithItemDtoForOwnerInBody() throws Exception {
        when(itemService.findAllByOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(pageItemDtoForOwner);

        mvc.perform(get(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(pageItemDtoForOwner.getContent())));
        verify(itemService, times(1)).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllByOwnerId - Плохой OwnerId")
    void getAllByOwnerId_whenInvalidOwnerId_thenResponseStatusStatusNotFound() throws Exception {
        when(itemService.findAllByOwnerId(anyLong(), any(Pageable.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(get(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAllByOwnerId - Плохой from или size")
    void getAllByOwnerId_whenInvalidFromOrSize_thenResponseStatusStatusBadRequest() throws Exception {
        when(itemService.findAllByOwnerId(anyLong(), any(Pageable.class)))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(get(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, times(1)).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод create - Успех")
    void create_whenValidItemIncomingDtoAndUser_thenResponseStatusOkWithItemDtoWithRequestIdInBody() throws Exception {
        when(itemService.create(any(ItemIncomingDto.class), anyLong()))
                .thenReturn(expectedItemDtoWithRequestId);

        mvc.perform(post(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemDtoWithRequestId)));
        verify(itemService, times(1)).create(any(ItemIncomingDto.class), anyLong());
    }

    @Test
    @DisplayName("Метод create - Плохой userId")
    void create_whenInvalidUserId_thenResponseStatusStatusNotFound() throws Exception {
        when(itemService.create(any(ItemIncomingDto.class), anyLong()))
                .thenThrow(NotFoundException.class);

        mvc.perform(post(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).create(any(ItemIncomingDto.class), anyLong());
    }

    @Test
    @DisplayName("Метод create - Плохие входные данные")
    void create_whenInvalidItemIncomingDto_thenResponseStatusStatusBadRequest() throws Exception {
        when(itemService.create(any(ItemIncomingDto.class), anyLong()))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(post(COMMON_ITEM_PATH)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(itemService, times(1)).create(any(ItemIncomingDto.class), anyLong());
    }

    @Test
    @DisplayName("Метод update - Успех")
    void update_whenValidItemIncomingDtoAndOwner_thenResponseStatusOkWithItemDtoWithRequestIdInBody() throws Exception {
        when(itemService.update(any(ItemIncomingDto.class), anyLong(), anyLong()))
                .thenReturn(expectedItemDtoWithRequestId);

        mvc.perform(patch(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedItemDtoWithRequestId)));
        verify(itemService, times(1)).update(any(ItemIncomingDto.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод update - Плохие входные данные")
    void update_whenInvalidItemIncomingDto_thenResponseStatusInternalServerError() throws Exception {
        when(itemService.update(any(ItemIncomingDto.class), anyLong(), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));

        mvc.perform(patch(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, times(1)).update(any(ItemIncomingDto.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод update - Запрос не от владельца")
    void update_whenNotOwner_thenResponseStatusForbidden() throws Exception {
        when(itemService.update(any(ItemIncomingDto.class), anyLong(), anyLong()))
                .thenThrow(ForbiddenException.class);

        mvc.perform(patch(COMMON_ITEM_PATH + ITEM_PREFIX, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
        verify(itemService, times(1)).update(any(ItemIncomingDto.class), anyLong(), anyLong());
    }

    @Test
    @DisplayName("Метод getAvailableByText - Успех")
    void getAvailableByText_whenInvoked_thenResponseStatusOkWithItemsDtoCollectionInBody() throws Exception {
        when(itemService.findAvailableByText(anyString(), any(Pageable.class)))
                .thenReturn(pageItemDtoForSearch);

        mvc.perform(get(COMMON_ITEM_PATH + SEARCH_PATH + SEARCH_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(pageItemDtoForSearch.getContent())));
        verify(itemService, times(1)).findAvailableByText(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод getAvailableByText - Плохой from или size")
    void getAvailableByText_whenInvalidFromOrSize_thenResponseStatusStatusBadRequest() throws Exception {
        when(itemService.findAvailableByText(anyString(), any(Pageable.class)))
                .thenThrow(ConstraintViolationException.class);

        mvc.perform(get(COMMON_ITEM_PATH + SEARCH_PATH + SEARCH_PREFIX)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, times(1)).findAvailableByText(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Метод createComment - Успех")
    void createComment_whenValidAllParams_thenResponseStatusOkWithCommentDtoInBody() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentIncomingDto.class)))
                .thenReturn(expectedCommentDto);

        mvc.perform(post(COMMON_ITEM_PATH + ITEM_PREFIX + COMMENT_PATH, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedCommentDto)));
        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any(CommentIncomingDto.class));
    }

    @Test
    @DisplayName("Метод createComment - Плохой userId или itemId")
    void createComment_whenInvalidUserIdOrItemId_thenResponseStatusNotFound() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentIncomingDto.class)))
                .thenThrow(NotFoundException.class);

        mvc.perform(post(COMMON_ITEM_PATH + ITEM_PREFIX + COMMENT_PATH, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any(CommentIncomingDto.class));
    }

    @Test
    @DisplayName("Метод createComment - Плохие входные данные")
    void createComment_whenInvalidCommentIncomingDto_thenResponseStatusBadRequest() throws Exception {
        when(itemService.createComment(anyLong(), anyLong(), any(CommentIncomingDto.class)))
                .thenThrow(InvalidConditionException.class);

        mvc.perform(post(COMMON_ITEM_PATH + ITEM_PREFIX + COMMENT_PATH, id)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(commentIncomingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(itemService, times(1)).createComment(anyLong(), anyLong(), any(CommentIncomingDto.class));
    }
}
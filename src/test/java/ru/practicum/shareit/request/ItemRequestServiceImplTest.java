package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemForItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository requestRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    long requestId = 1;
    long userId = 1;
    long requesterId = 2;
    User user = User.builder().id(userId).name("UserName").email("test@test.com").build();
    User requester = User.builder().id(requesterId).name("RequesterName").email("requester@test.com").build();

    long itemId = 1;
    Item item = Item.builder()
            .id(itemId)
            .name("TestItemName")
            .description("ItemDescription")
            .available(true)
            .owner(user)
            .requestId(requestId)
            .build();
    Collection<Item> collectionItem = Collections.singletonList(item);

    ItemForItemRequestDto itemDto = ItemForItemRequestDto.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .ownerId(item.getOwner().getId())
            .available(item.getAvailable())
            .requestId(requestId)
            .build();
    Collection<ItemForItemRequestDto> collectionItemForRequestDto = Collections.singletonList(itemDto);

    LocalDateTime created = LocalDateTime.now().plusMinutes(5);
    ItemRequest request = ItemRequest.builder()
            .id(requestId)
            .description("TestRequestDescription")
            .requester(requester)
            .created(created)
            .build();
    List<ItemRequest> expectedCollectionRequest = Collections.singletonList(request);

    ItemRequestDto expectedRequestDto = ItemRequestDto.builder()
            .id(request.getId())
            .description(request.getDescription())
            .created(request.getCreated())
            .items(collectionItemForRequestDto)
            .build();
    Collection<ItemRequestDto> expectedCollectionRequestDto = Collections.singletonList(expectedRequestDto);

    ItemRequestShortDto expectedRequestShortDto = ItemRequestShortDto.builder()
            .id(request.getId())
            .description(request.getDescription())
            .created(request.getCreated())
            .build();
    ItemRequestIncomingDto requestIncomingDto = ItemRequestIncomingDto.builder()
            .description(request.getDescription())
            .build();

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);
    Page<ItemRequest> pageItemRequest = new PageImpl<>(
            Collections.singletonList(request), pageable, 1);
    Page<ItemRequestDto> pageItemRequestDto = new PageImpl<>(
            Collections.singletonList(expectedRequestDto), pageable, 1);

    String exceptionMessage = "Message";

    @Test
    void findById_WhenItemRequestFound_thenReturnedItemRequestDto() {
        when(userRepository.extract(anyLong())).thenReturn(user);
        when(requestRepository.extract(anyLong())).thenReturn(request);
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(collectionItem);

        assertEquals(expectedRequestDto, requestService.findById(userId, requestId));

        verify(userRepository, times(1)).extract(anyLong());
        verify(requestRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
    }

    @Test
    void findById_WhenUserNotFound_thenNotReturnedItemRequestDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> requestService.findById(userId, requestId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findById_WhenItemRequestNotFound_thenNotReturnedItemRequestDto() {
        when(userRepository.extract(anyLong())).thenReturn(user);
        when(requestRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> requestService.findById(userId, requestId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verify(requestRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository, requestRepository);
    }

    @Test
    void findAllByRequesterId_WhenRequesterFound_thenReturnedItemRequestDtoCollection() {
        when(userRepository.extract(anyLong())).thenReturn(requester);
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(collectionItem);
        when(requestRepository.findAllByRequesterIdOrderByCreatedDesc(anyLong()))
                .thenReturn(expectedCollectionRequest);

        assertEquals(expectedCollectionRequestDto, requestService.findAllByRequesterId(requesterId));

        verify(userRepository, times(1)).extract(anyLong());
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verify(requestRepository, times(1)).findAllByRequesterIdOrderByCreatedDesc(anyLong());
        verifyNoMoreInteractions(userRepository, itemRepository, requestRepository);
    }

    @Test
    void findAllByRequesterId_WhenRequesterNotFound_thenNotReturnedItemRequestDtoCollection() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> requestService.findAllByRequesterId(requesterId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAllByExpectRequesterId_whenRequesterFound_thenReturnedPageItemRequestDto() {
        when(userRepository.extract(anyLong())).thenReturn(user);
        when(requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(anyLong(), any(Pageable.class)))
                .thenReturn(pageItemRequest);
        when(itemRepository.findAllByRequestId(anyLong())).thenReturn(collectionItem);

        assertEquals(pageItemRequestDto, requestService.findAllByExpectRequesterId(userId, pageable));

        verify(userRepository, times(1)).extract(anyLong());
        verify(requestRepository, times(1)).findAllByRequesterIdNotOrderByCreatedDesc(anyLong(), any(Pageable.class));
        verify(itemRepository, times(1)).findAllByRequestId(anyLong());
        verifyNoMoreInteractions(userRepository, requestRepository, itemRepository);
    }

    @Test
    void findAllByExpectRequesterId_whenRequesterNotFound_thenNotReturnedPageItemRequestDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () ->
                        requestService.findAllByExpectRequesterId(userId, pageable));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void create_whenItemRequestCreated_thenReturnedItemRequestShortDto() {
        when(userRepository.extract(anyLong())).thenReturn(requester);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);

        assertEquals(expectedRequestShortDto, requestService.create(requestIncomingDto, requesterId));

        verify(userRepository, times(1)).extract(anyLong());
        verify(requestRepository, times(1)).save(any(ItemRequest.class));
        verifyNoMoreInteractions(userRepository, requestRepository);
    }

    @Test
    void create_whenRequesterNotFound_thenNotReturnedItemRequestShortDto() {
        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> requestService.create(requestIncomingDto, requesterId));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
        verifyNoMoreInteractions(userRepository);
    }

}
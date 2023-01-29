package ru.practicum.shareit.request;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.pagination.PaginationConstant.SORT_CREATED_DESC;

@Transactional
@SpringBootTest
class ItemRequestServiceImplTestIT {
    private static final long UNKNOWN_ID = Long.MAX_VALUE;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRequestServiceImpl requestService;

    @Autowired
    private EntityManager em;

    UserDto owner = UserDto.builder().name("OwnerName").email("owner@test.com").build();
    UserDto requester = UserDto.builder().name("RequesterName").email("requester@test.com").build();
    UserDto user = UserDto.builder().name("UserName").email("user@test.com").build();

    ItemIncomingDto itemIncomingDto = ItemIncomingDto.builder()
            .name("ItemName")
            .description("ItemDescription")
            .available(true)
            .build();

    ItemRequestIncomingDto requestIncomingDto =
            ItemRequestIncomingDto.builder().description("RequestDescription").build();

    String exceptionMessageNotUser = "Запрос на несуществующего пользователя с id = ";
    String exceptionMessageNotItemRequest = "Запрос на несуществующий для поиска нужной вещи запрос с id = ";

    int from = 1;
    int size = 10;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, SORT_CREATED_DESC);

    @Test
    void create() {
        UserDto resultUser = userService.create(user);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> requestService.create(requestIncomingDto, UNKNOWN_ID));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        ItemRequestShortDto resultRequest = requestService.create(requestIncomingDto, resultUser.getId());

        ItemRequest savedRequest = em
                .createQuery("select ir from ItemRequest ir where ir.id = :id", ItemRequest.class)
                .setParameter("id", resultRequest.getId())
                .getSingleResult();

        assertThat(savedRequest.getDescription(), equalTo(resultRequest.getDescription()));
        assertThat(savedRequest.getCreated(), equalTo(resultRequest.getCreated()));
    }

    @Test
    void findById() {
        UserDto resultUser = userService.create(user);
        UserDto resultOwner = userService.create(owner);
        ItemRequestShortDto resultRequest = requestService.create(requestIncomingDto, resultUser.getId());
        ItemDto resultItem = itemService.create(
                itemIncomingDto.toBuilder().requestId(resultRequest.getId()).build(),
                resultOwner.getId());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> requestService.findById(UNKNOWN_ID, resultRequest.getId()));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        exception = assertThrows(
                NotFoundException.class, () -> requestService.findById(resultUser.getId(), UNKNOWN_ID));
        assertEquals(exceptionMessageNotItemRequest + UNKNOWN_ID, exception.getMessage());

        ItemRequestDto savedRequest = requestService.findById(resultUser.getId(), resultRequest.getId());

        assertThat(savedRequest.getDescription(), equalTo(resultRequest.getDescription()));
        assertThat(savedRequest.getCreated(), equalTo(resultRequest.getCreated()));
        assertThat(1, equalTo(savedRequest.getItems().size()));
        assertThat(resultItem.getId(), equalTo(savedRequest.getItems().iterator().next().getId()));
        assertThat(resultItem.getName(), equalTo(savedRequest.getItems().iterator().next().getName()));
        assertThat(resultItem.getDescription(), equalTo(savedRequest.getItems().iterator().next().getDescription()));
        assertThat(resultItem.getAvailable(), equalTo(savedRequest.getItems().iterator().next().getAvailable()));
        assertThat(resultOwner.getId(), equalTo(savedRequest.getItems().iterator().next().getOwnerId()));
        assertThat(resultRequest.getId(), equalTo(savedRequest.getItems().iterator().next().getRequestId()));
    }

    @Test
    void findAllByRequesterId() {
        UserDto resultRequester = userService.create(requester);
        UserDto resultUser = userService.create(user);
        ItemRequestShortDto resultRequest = requestService.create(requestIncomingDto, resultRequester.getId());
        ItemRequestShortDto resultRequest2 = requestService.create(requestIncomingDto, resultRequester.getId());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> requestService.findAllByRequesterId(UNKNOWN_ID));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        List<ItemRequestDto> result = requestService.findAllByRequesterId(resultRequester.getId());

        assertThat(2, equalTo(result.size()));
        assertThat(resultRequest2.getId(), equalTo(result.get(0).getId()));
        assertThat(resultRequest.getId(), equalTo(result.get(1).getId()));

        List<ItemRequestDto> resultEmpty = requestService.findAllByRequesterId(resultUser.getId());

        assertThat(0, equalTo(resultEmpty.size()));
    }

    @Test
    void findAllByExpectRequesterId() {
        UserDto resultRequester = userService.create(requester);
        UserDto resultUser = userService.create(user);
        UserDto resultOwner = userService.create(owner);
        ItemRequestShortDto resultRequest = requestService.create(requestIncomingDto, resultRequester.getId());
        ItemRequestShortDto resultRequest2 = requestService.create(requestIncomingDto, resultOwner.getId());
       requestService.create(requestIncomingDto, resultUser.getId());
        ItemRequestShortDto resultRequest4 = requestService.create(requestIncomingDto, resultRequester.getId());

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> requestService.findAllByExpectRequesterId(UNKNOWN_ID, pageable));
        assertEquals(exceptionMessageNotUser + UNKNOWN_ID, exception.getMessage());

        ItemRequestDto savedRequest = requestService.findById(resultRequester.getId(), resultRequest.getId());
        ItemRequestDto savedRequest2 = requestService.findById(resultOwner.getId(), resultRequest2.getId());
        ItemRequestDto savedRequest4 = requestService.findById(resultRequester.getId(), resultRequest4.getId());

        Page<ItemRequestDto> result = requestService.findAllByExpectRequesterId(resultUser.getId(), pageable);

        assertThat(3, equalTo(result.getContent().size()));
        assertThat(result.getContent(), Matchers.containsInRelativeOrder(
                savedRequest4, savedRequest2, savedRequest));
    }

}
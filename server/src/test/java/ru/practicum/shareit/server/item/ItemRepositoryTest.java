package ru.practicum.shareit.server.item;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.pagination.PaginationUtils;
import ru.practicum.shareit.server.request.ItemRequestRepository;
import ru.practicum.shareit.server.request.model.ItemRequest;
import ru.practicum.shareit.server.user.UserRepository;
import ru.practicum.shareit.server.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.server.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    int from = 0;
    int size = 10;
    User saveUser;
    Item saveItem;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);

    @BeforeEach
    private void addItems() {

        User user = User.builder().name("TestName").email("test@test.com").build();
        saveUser = userRepository.save(user);
        saveItem = itemRepository.save(Item.builder()
                .name("TestItemName")
                .description("ItemDescription")
                .available(true)
                .owner(saveUser)
                .build());
    }

    @Test
    void extract() {
        assertEquals(saveItem, itemRepository.extract(saveItem.getId()));
        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> itemRepository.extract(saveItem.getId() + 1));
        assertEquals("Запрос на несуществующую вещь с id = " + (saveItem.getId() + 1), exception.getMessage());
    }

    @Test
    void findAllByOwnerId() {
        assertEquals(1, itemRepository.findAllByOwnerId(saveUser.getId(), pageable).getContent().size());
        assertEquals(0, itemRepository.findAllByOwnerId(saveUser.getId() + 1, pageable).getContent().size());
    }

    @Test
    void findAvailableByText() {
        Item saveItem2 = itemRepository.save(Item.builder()
                .name("TestItem")
                .description("Item_nAmE_Description")
                .available(true)
                .owner(saveUser)
                .build());
        itemRepository.save(Item.builder()
                .name("TestItemName")
                .description("ItemDescription")
                .available(false)
                .owner(saveUser)
                .build());

        assertEquals(2, itemRepository.findAvailableByText("NAME", pageable).getContent().size());
        assertEquals(saveItem, itemRepository.findAvailableByText("NAME", pageable).getContent().get(0));
        assertEquals(saveItem2, itemRepository.findAvailableByText("NAME", pageable).getContent().get(1));
        assertEquals(0, itemRepository.findAvailableByText("alien", pageable).getContent().size());
    }

    @Test
    void findAllByRequestId() {
        ItemRequest request = ItemRequest.builder()
                .description("RequestDescription")
                .requester(saveUser)
                .created(LocalDateTime.now().plusMinutes(5))
                .build();
        ItemRequest saveRequest = requestRepository.save(request);
        Item saveItem2 = itemRepository.save(Item.builder()
                .name("TestItem")
                .description("Item_nAmE_Description")
                .available(true)
                .owner(saveUser)
                .requestId(saveRequest.getId())
                .build());

        assertEquals(1, itemRepository.findAllByRequestId(saveRequest.getId()).size());
        assertEquals(Collections.singletonList(saveItem2), itemRepository.findAllByRequestId(saveRequest.getId()));
        assertEquals(0, itemRepository.findAllByRequestId(saveRequest.getId() + 1).size());
    }

    @AfterEach
    private void deleteItems() {
        itemRepository.deleteAll();
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }
}

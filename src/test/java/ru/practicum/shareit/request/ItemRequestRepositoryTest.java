package ru.practicum.shareit.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.pagination.PaginationUtils;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.pagination.PaginationConstant.DEFAULT_PAGINATION_SORT;

@DataJpaTest
class ItemRequestRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    int from = 1;
    int size = 10;
    User saveRequester;
    User saveRequester2;

    LocalDateTime created = LocalDateTime.now().plusMinutes(5);
    ItemRequest saveRequest;
    ItemRequest saveRequest2;
    ItemRequest saveRequest3;
    ItemRequest saveRequest4;
    Pageable pageable = PageRequest.of(PaginationUtils.getCalculatedPage(from, size), size, DEFAULT_PAGINATION_SORT);


    @BeforeEach
    private void addItems() {
        saveRequester = userRepository.save(
                User.builder().name("RequesterName").email("requester@test.com").build());
        saveRequester2 = userRepository.save(
                User.builder().name("Requester2Name").email("requester2@test.com").build());
        saveRequest = requestRepository.save(ItemRequest.builder()
                .description("TestRequestDescription")
                .requester(saveRequester)
                .created(created)
                .build());
        saveRequest2 = requestRepository.save(ItemRequest.builder()
                .description("TestRequest2Description")
                .requester(saveRequester2)
                .created(created)
                .build());
        saveRequest3 = requestRepository.save(ItemRequest.builder()
                .description("TestRequest3Description")
                .requester(saveRequester)
                .created(created.plusMinutes(60))
                .build());
        saveRequest4 = requestRepository.save(ItemRequest.builder()
                .description("TestRequest42Description")
                .requester(saveRequester2)
                .created(created.plusMinutes(60))
                .build());
    }

    @Test
    void extract() {
        assertEquals(saveRequest, requestRepository.extract(saveRequest.getId()));
        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> requestRepository.extract(saveRequest.getId() + 100));
        assertEquals("Запрос на несуществующий для поиска нужной вещи запрос с id = " + (saveRequest.getId() + 100),
                exception.getMessage());
    }

    @Test
    void findAllByRequesterIdOrderByCreatedDesc() {
        List<ItemRequest> result =
                requestRepository.findAllByRequesterIdOrderByCreatedDesc(saveRequester.getId());
        assertEquals(2, result.size());
        assertEquals(saveRequest3, result.get(0));
        assertEquals(saveRequest, result.get(1));
    }

    @Test
    void findAllByRequesterIdNotOrderByCreatedDesc() {
        Page<ItemRequest> result = requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(
                saveRequester.getId(), pageable);
        assertEquals(2, result.getContent().size());
        assertEquals(saveRequest4, result.getContent().get(0));
        assertEquals(saveRequest2, result.getContent().get(1));
    }

    @AfterEach
    private void deleteItems() {
        userRepository.deleteAll();
        requestRepository.deleteAll();
    }
}
package ru.practicum.shareit.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    ItemRequestRepository requestRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public ItemRequestDto findById(long userId, long id) {
        userRepository.extract(userId);
        ItemRequest itemRequest = requestRepository.extract(id);
        return ItemRequestMapper.toItemRequestDto(
                itemRequest,
                itemRepository.findAllByRequestId(id)
        );
    }

    @Override
    public List<ItemRequestDto> findAllByRequesterId(long requesterId) {
        userRepository.extract(requesterId);
        return requestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterId)
                .stream()
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(
                        itemRequest,
                        itemRepository.findAllByRequestId(requesterId)))
                .collect(Collectors.toList());
    }

    @Override
    public Page<ItemRequestDto> findAllByExpectRequesterId(long requesterId, Pageable pageable) {
        userRepository.extract(requesterId);
        return requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(requesterId, pageable)
                .map(itemRequest -> ItemRequestMapper.toItemRequestDto(
                        itemRequest,
                        itemRepository.findAllByRequestId(itemRequest.getId())
                ));
    }

    @Transactional
    @Override
    public ItemRequestShortDto create(ItemRequestIncomingDto itemRequestDto, long userId) {
        User user = userRepository.extract(userId);
        ItemRequest newItemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, user);
        ItemRequest createdItemRequest = requestRepository.save(newItemRequest);
        log.info("Добавлен для нужной вещи запрос с id = {} для пользователя с id = {}",
                createdItemRequest.getId(), userId);
        return ItemRequestMapper.toItemRequestShortDto(createdItemRequest);
    }

}

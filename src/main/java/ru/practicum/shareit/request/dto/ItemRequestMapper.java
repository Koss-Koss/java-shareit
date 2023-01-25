package ru.practicum.shareit.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(
            ItemRequest itemRequestStorage, Collection<Item> items) {
        return ItemRequestDto.builder()
                .id(itemRequestStorage.getId())
                .description(itemRequestStorage.getDescription())
                .created(itemRequestStorage.getCreated())
                .items(items.stream()
                        .map(ItemMapper::toItemForItemRequestDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public static ItemRequestShortDto toItemRequestShortDto(ItemRequest itemRequestStorage) {
        return ItemRequestShortDto.builder()
                .id(itemRequestStorage.getId())
                .description(itemRequestStorage.getDescription())
                .created(itemRequestStorage.getCreated())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestIncomingDto itemRequestDto, User user) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(user)
                .created(LocalDateTime.now())
                .build();
    }
}

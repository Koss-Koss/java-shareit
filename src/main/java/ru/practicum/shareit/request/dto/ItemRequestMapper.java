package ru.practicum.shareit.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.model.ItemRequest;

@Component
public final class ItemRequestMapper {
    public ItemRequestDto toItemRequestDto(ItemRequest itemRequestStorage) {
        return ItemRequestDto.builder()
                .id(itemRequestStorage.getId())
                .description(itemRequestStorage.getDescription())
                .requester(itemRequestStorage.getRequester())
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .requester(itemRequestDto.getRequester())
                .build();
    }
}

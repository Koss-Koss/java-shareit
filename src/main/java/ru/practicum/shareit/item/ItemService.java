package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemIncomingDto;

import java.util.Collection;

public interface ItemService {
    ItemDto findById(long userId, long id);

    Collection<ItemDto> findAllByOwnerId(long ownerId);

    ItemDto create(ItemIncomingDto itemDto, long userId);

    ItemDto update(ItemIncomingDto itemDto, long itemId, long userId);

    void delete(long itemId, long userId);

    Collection<ItemDto> findAvailableByText(String text);
}

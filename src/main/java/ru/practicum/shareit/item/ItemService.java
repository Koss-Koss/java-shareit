package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto findById(long id);

    Collection<ItemDto> findAllByOwnerId(long ownerId);

    ItemDto create(ItemDto itemDto, long userId);

    ItemDto update(ItemDto itemDto, long itemId, long userId);

    void delete(long itemId, long userId);

    Collection<ItemDto> findAvailableByText(String text);
}

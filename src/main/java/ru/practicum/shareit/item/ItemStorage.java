package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemStorage {
    boolean contains(long id);

    Item findById(long id);

    Collection<Item> findAllByOwnerId(long ownerId);

    Item create(Item item);

    Item update(long id, Item item);

    void delete(long id);

    void deleteAllByOwnerId(long ownerId);

    Collection<Item> findAvailableByText(String text);
}

package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private long currentId = 1;

    @Override
    public boolean contains(long id) {
        return items.containsKey(id);
    }

    @Override
    public Item findById(long id) {
        return items.get(id);
    }

    @Override
    public Collection<Item> findAllByOwnerId(long ownerId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwnerId() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public Item create(Item item) {
        long id = currentId++;
        Item createdItem = item.toBuilder()
                .id(id)
                .build();
        items.put(id, createdItem);
        log.info("Вещь id={} создана", id);
        return createdItem;
    }

    @Override
    public Item update(long id, Item item) {
        Item.ItemBuilder builder = findById(id).toBuilder();
        String itemName = item.getName();
        if (itemName != null) {
            builder.name(itemName);
        }
        String itemDescription = item.getDescription();
        if (itemDescription != null) {
            builder.description(itemDescription);
        }
        Boolean itemAvailable = item.getAvailable();
        if (itemAvailable != null) {
            builder.available(itemAvailable);
        }
        Item updatedItem = builder.build();
        items.put(id, updatedItem);
        log.info("Вещь id={} изменена", id);
        return updatedItem;
    }

    @Override
    public void delete(long id) {
        items.remove(id);
        log.info("Вещь id={} удалена", id);
    }

    @Override
    public void deleteAllByOwnerId(long ownerId) {
        items.entrySet().removeIf(entry -> entry.getValue().getOwnerId() == ownerId);
        log.info("Удалены все вещи пользователя id={} создана", ownerId);
    }

    @Override
    public Collection<Item> findAvailableByText(String text) {
        if(text.isEmpty()) {
            return new ArrayList<>();
        }
        String lowerText = text.toLowerCase();
        return items.values()
                .stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerText)
                                || item.getDescription().toLowerCase().contains(lowerText))
                )
                .collect(Collectors.toList());
    }
}

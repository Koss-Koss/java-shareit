package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
public class ItemMapper {
    public static ItemDto toItemDto(Item itemStorage) {
        return ItemDto.builder()
                .id(itemStorage.getId())
                .name(itemStorage.getName())
                .description(itemStorage.getDescription())
                .available(itemStorage.getAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto, User user) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .build();
    }
}

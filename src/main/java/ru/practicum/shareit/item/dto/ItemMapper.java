package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

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

    public static Item toItem(ItemDto itemDto, long ownerId) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .ownerId(ownerId)
                .build();
    }
}

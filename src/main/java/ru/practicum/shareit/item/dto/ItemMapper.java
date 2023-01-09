package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

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

    public static ItemDto toItemDto(Item itemStorage, Collection<Comment> comments) {
        ItemDto itemDto = toItemDto(itemStorage);
        itemDto.setComments(comments
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    public static ItemDto toItemDto(Item itemStorage,
                                    BookingShortDto lastBooking,
                                    BookingShortDto nextBooking,
                                    Collection<Comment> comments) {
        ItemDto itemDto = toItemDto(itemStorage, comments);
        if (lastBooking != null) {
            itemDto.setLastBooking(lastBooking);
        }
        if (nextBooking != null) {
            itemDto.setNextBooking(nextBooking);
        }
        return itemDto;
    }

    public static Item toItem(ItemIncomingDto itemDto, User user) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .build();
    }
}

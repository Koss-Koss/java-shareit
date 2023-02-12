package ru.practicum.shareit.server.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.server.booking.dto.BookingShortDto;
import ru.practicum.shareit.server.item.model.Comment;
import ru.practicum.shareit.server.item.model.Item;
import ru.practicum.shareit.server.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@UtilityClass
public class ItemMapper {
    public static ItemDto toItemDto(Item itemStorage) {
        return ItemDto.builder()
                .id(itemStorage.getId())
                .name(itemStorage.getName())
                .description(itemStorage.getDescription())
                .available(itemStorage.getAvailable())
                .requestId(itemStorage.getRequestId())
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

    public static ItemForItemRequestDto toItemForItemRequestDto(Item itemStorage) {
        return ItemForItemRequestDto.builder()
                .id(itemStorage.getId())
                .name(itemStorage.getName())
                .description(itemStorage.getDescription())
                .ownerId(itemStorage.getOwner().getId())
                .available(itemStorage.getAvailable())
                .requestId(itemStorage.getRequestId())
                .build();
    }

    public static Item toItem(ItemIncomingDto itemDto, User user) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .requestId(itemDto.getRequestId())
                .build();
    }
}

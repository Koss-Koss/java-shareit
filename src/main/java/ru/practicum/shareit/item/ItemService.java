package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentIncomingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemIncomingDto;

public interface ItemService {
    ItemDto findById(long userId, long id);

    Page<ItemDto> findAllByOwnerId(long ownerId, long from, Pageable pageable);

    ItemDto create(ItemIncomingDto itemDto, long userId);

    ItemDto update(ItemIncomingDto itemDto, long itemId, long userId);

    void delete(long itemId, long userId);

    Page<ItemDto> findAvailableByText(String text, long from, Pageable pageable);

    CommentDto createComment(long authorId, long id, CommentIncomingDto commentDto);
}

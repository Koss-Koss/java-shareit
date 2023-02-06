package ru.practicum.shareit.server.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.server.item.dto.*;

public interface ItemService {

    ItemDto findById(long userId, long id);

    Page<ItemDto> findAllByOwnerId(long ownerId, Pageable pageable);

    ItemDto create(ItemIncomingDto itemDto, long userId);

    ItemDto update(ItemIncomingDto itemDto, long itemId, long userId);

    Page<ItemDto> findAvailableByText(String text, Pageable pageable);

    CommentDto createComment(long authorId, long id, CommentIncomingDto commentDto);
}

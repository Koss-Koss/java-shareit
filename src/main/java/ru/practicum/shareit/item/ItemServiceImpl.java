package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.InvalidConditionException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.*;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ItemServiceImpl implements ItemService {
    CommentRepository commentRepository;
    BookingRepository bookingRepository;
    ItemRepository itemRepository;
    UserRepository userRepository;

    @Override
    public ItemDto findById(long userId, long id) {
        Item item = itemRepository.extract(id);
        if (item.getOwner().getId() == userId) {
            return ItemMapper.toItemDto(
                    item,
                    BookingMapper.toBookingShortDto(findLastBooking(item.getId())),
                    BookingMapper.toBookingShortDto(findNextBooking(item.getId())),
                    findComments(id)
            );
        }
        return ItemMapper.toItemDto(
                item,
                findComments(id)
        );
    }

    @Override
    public Page<ItemDto> findAllByOwnerId(long ownerId, Pageable pageable) {
        userRepository.extract(ownerId);
        return itemRepository.findAllByOwnerId(ownerId, pageable)
                .map(item -> ItemMapper.toItemDto(
                        item,
                        BookingMapper.toBookingShortDto(findLastBooking(item.getId())),
                        BookingMapper.toBookingShortDto(findNextBooking(item.getId())),
                        findComments(item.getId())
                ));
    }

    @Transactional
    @Override
    public ItemDto create(ItemIncomingDto itemDto, long userId) {
        User user = userRepository.extract(userId);
        Item newItem = ItemMapper.toItem(itemDto, user);
        Item createdItem = itemRepository.save(newItem);
        log.info("Добавлена вещь с id = {} для пользователя с id = {}", createdItem.getId(), userId);
        return ItemMapper.toItemDto(createdItem);
    }

    @Transactional
    @Override
    public ItemDto update(ItemIncomingDto itemDto, long itemId, long userId) {
        User user = userRepository.extract(userId);
        Item currentItem = itemRepository.extract(itemId);
        if (!currentItem.getOwner().equals(user)) {
            throw new ForbiddenException("Не совпадают id пользователя из запроса и владельца вещи. " +
                    "Только владелец может изменять/удалять вещь");
        }
        String itemName = itemDto.getName();
        if (itemName != null) {
            currentItem.setName(itemName);
        }
        String itemDescription = itemDto.getDescription();
        if (itemDescription != null) {
            currentItem.setDescription(itemDescription);
        }
        Boolean itemAvailable = itemDto.getAvailable();
        if (itemAvailable != null) {
            currentItem.setAvailable(itemAvailable);
        }
        Item updatedItem = itemRepository.save(currentItem);
        log.info("Обновлена вещь с id = {} для пользователя с id = {}", updatedItem.getId(), userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public Page<ItemDto> findAvailableByText(String text, Pageable pageable) {
        if (text.isEmpty()) {
            return new PageImpl<>(Collections.emptyList());
        }
        Page<ItemDto> itemDtos = itemRepository.findAvailableByText(text, pageable)
                .map(ItemMapper::toItemDto);
        log.info((itemDtos.isEmpty() ? "Не найдены" : "Найдены") +
                " вещи, имя или описание которых содержат строку = {}", text);
        return itemDtos;
    }

    @Transactional
    @Override
    public CommentDto createComment(long userId, long itemId, CommentIncomingDto commentDto) {
        User author = userRepository.extract(userId);
        Item item = itemRepository.extract(itemId);
        if (!isAuthorUsedItem(userId, itemId)) {
            throw new InvalidConditionException("Запрещены комментарии пользователей, не арендовавших вещь");
        }
        Comment newComment = CommentMapper.toComment(commentDto, author, item);
        Comment createdComment = commentRepository.save(newComment);
        log.info("Добавлен комментарий id = {} дла вещи с id = {} пользователем с id = {}",
                createdComment.getId(), itemId, userId);
        return CommentMapper.toCommentDto(createdComment);
    }

    protected Booking findLastBooking(long itemId) {
        return bookingRepository.findFirstByItemIdAndEndLessThanOrderByStartDesc(itemId, LocalDateTime.now());
    }

    protected Booking findNextBooking(long itemId) {
        return bookingRepository.findFirstByItemIdAndStartGreaterThanOrderByStartAsc(itemId, LocalDateTime.now());
    }

    private boolean isAuthorUsedItem(long authorId, long itemId) {
        int count = bookingRepository.countCompletedBookings(authorId, itemId, LocalDateTime.now());
        return count > 0;
    }

    protected Collection<Comment> findComments(long itemId) {
        return commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId);
    }

}

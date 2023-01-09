package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemIncomingDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto findById(long userId, long id) {

        Item item = itemRepository.extract(id);
        if (item.getOwner().getId() == userId) {
            return ItemMapper.toItemDto(
                    item,
                    BookingMapper.bookingShortDto(findLastBooking(item.getId())),
                    BookingMapper.bookingShortDto(findNextBooking(item.getId()))
                    // добавить комменты
            );
        }
        return ItemMapper.toItemDto(
                item
                // добавить комменты
        );
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(long ownerId) {
        userRepository.extract(ownerId);
        Collection<ItemDto> itemDtos = itemRepository.findAllByOwnerId(ownerId)
                .stream()
                .map(item -> ItemMapper.toItemDto(
                        item,
                        BookingMapper.bookingShortDto(findLastBooking(item.getId())),
                        BookingMapper.bookingShortDto(findNextBooking(item.getId()))
                        // комменты
                ))
                .collect(Collectors.toList());
        return itemDtos;
    }

    @Override
    public ItemDto create(ItemIncomingDto itemDto, long userId) {
        User user = userRepository.extract(userId);
        Item newItem = ItemMapper.toItem(itemDto, user);
        itemRepository.save(newItem);
        log.info("Добавлена вещь с id = {} для пользователя с id = {}", newItem.getId(), userId);
        return ItemMapper.toItemDto(newItem);
    }

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
    public void delete(long itemId, long userId) {
        userRepository.extract(userId);
        itemRepository.extract(itemId);
        itemRepository.deleteById(itemId);
        log.info("Удалена вещь с id = {} для пользователя с id = {}", itemId, userId);
    }

    @Override
    public Collection<ItemDto> findAvailableByText(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        Collection<ItemDto> itemDtos = itemRepository.findAvailableByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.info((itemDtos.isEmpty() ? "Не найдены" : "Найдены") +
                " вещи, имя или описание которых содержат строку = {}", text);
        return itemDtos;
    }

    private Booking findLastBooking(long itemId) {
        return bookingRepository.findLastForItem(itemId, LocalDateTime.now());
    }

    private Booking findNextBooking(long itemId) {
        return bookingRepository.findNextForItem(itemId, LocalDateTime.now());
    }

}

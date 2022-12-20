package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserValidationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final ItemValidationService itemValidationService;
    private final UserValidationService userValidationService;

    @Override
    public ItemDto findById(long id) {
        itemValidationService.validateItemId(id);
        return ItemMapper.toItemDto(itemStorage.findById(id));
    }

    @Override
    public Collection<ItemDto> findAllByOwnerId(long ownerId) {
        userValidationService.validateUserId(ownerId);
        itemStorage.findAllByOwnerId(ownerId);
        Collection<ItemDto> itemDtos = itemStorage.findAllByOwnerId(ownerId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return itemDtos;
    }

    @Override
    public ItemDto create(ItemDto itemDto, long userId) {
        userValidationService.validateUserId(userId);
        return ItemMapper.toItemDto(itemStorage.create(ItemMapper.toItem(itemDto, userId)));
    }

    @Override
    public ItemDto update(ItemDto itemDto, long itemId, long userId) {
        itemValidationService.validateItemId(itemId);
        userValidationService.validateUserId(userId);
        itemValidationService.validateItemOwnerId(itemId, userId);
        return ItemMapper.toItemDto(itemStorage.update(itemId, ItemMapper.toItem(itemDto, userId)));
    }

    @Override
    public void delete(long itemId, long userId) {
        itemValidationService.validateItemId(itemId);
        userValidationService.validateUserId(userId);
        itemValidationService.validateItemOwnerId(itemId, userId);
        itemStorage.delete(itemId);
    }

    @Override
    public Collection<ItemDto> findAvailableByText(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        Collection<ItemDto> itemDtos = itemStorage.findAvailableByText(text)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        return itemDtos;
    }

}

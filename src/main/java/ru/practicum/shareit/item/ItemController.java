package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("{itemId}")
    public ItemDto getItemById(@PathVariable long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getAllByOwnerId(@RequestHeader("X-Sharer-User-Id") long ownerId) {
        return itemService.findAllByOwnerId(ownerId);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, itemId, userId);
    }

    @DeleteMapping("{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long itemId) {
        itemService.delete(itemId, userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> getAvailableByText(@RequestParam String text) {
        return itemService.findAvailableByText(text);
    }
}

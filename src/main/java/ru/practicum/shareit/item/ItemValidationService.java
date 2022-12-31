package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;

@Component
@RequiredArgsConstructor
public class ItemValidationService {
    private final ItemStorage itemStorage;

    protected void validateItemId(long id) {
        if (id <= 0) {
            throw new NotFoundException("id вещи не может быть отрицательным или равным нулю");
        }
        if (!itemStorage.contains(id)) {
            throw new NotFoundException("При запросе по id должна существовать вещь с указанным id");
        }
    }

    protected void validateItemOwnerId(long itemId, long userId) {
        if (itemStorage.findById(itemId).getOwnerId() != userId) {
            throw new ForbiddenException("Не совпадают id пользователя из запроса и владельца вещи. " +
                    "Только владелец может изменять/удалять вещь");
        }
    }
}

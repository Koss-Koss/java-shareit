package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.DuplicateUserEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

@Component
@RequiredArgsConstructor
public class UserValidationService {
    private final UserStorage userStorage;

    public void validateUserId(long id) {
        if (id <= 0) {
            throw new NotFoundException("id пользователя не может быть отрицательным или равным нулю");
        }
        if (!userStorage.contains(id)) {
            throw new NotFoundException("При запросе по id должен существовать пользователь с указанным id");
        }
    }

    protected void validateUserEmail(UserDto userDto) {
        if (userStorage.findAll()
                .stream()
                .anyMatch(
                        s -> s.getEmail().equalsIgnoreCase(userDto.getEmail())
                )
        ) {
            throw new DuplicateUserEmailException("email: " + userDto.getEmail()
                    + " уже используется другим пользователем");
        }
    }

    protected void validateUserEmail(long id, UserDto userDto) {
        if (userStorage.findAll()
                .stream()
                .anyMatch(
                        s -> s.getEmail().equalsIgnoreCase(userDto.getEmail())
                                && s.getId() != id
                )
        ) {
            throw new DuplicateUserEmailException("email: " + userDto.getEmail()
                    + " уже используется другим пользователем");
        }
    }
}

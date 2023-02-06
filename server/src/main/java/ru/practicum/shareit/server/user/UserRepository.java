package ru.practicum.shareit.server.user;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    default User extract(long id) {
        return findById(id).orElseThrow(
                () -> new NotFoundException("Запрос на несуществующего пользователя с id = " + id));
    }
}

package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserStorage {
    boolean contains(long id);

    User findById(long id);

    Collection<User> findAll();

    User create(User user);

    User update(long id, User user);

    void delete(long id);
}

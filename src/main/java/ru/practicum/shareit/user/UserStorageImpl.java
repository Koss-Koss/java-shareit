package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
@Slf4j
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 1;

    @Override
    public boolean contains(long id) {
        return users.containsKey(id);
    }

    @Override
    public User findById(long id) {
        return users.get(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        long id = currentId++;
        User createdUser = user.toBuilder()
                .id(id)
                .build();
        users.put(id, createdUser);
        log.info("Пользователь id={} создан", id);
        return createdUser;
    }

    @Override
    public User update(long id, User user) {
        User.UserBuilder builder = findById(id).toBuilder();
        String userName = user.getName();
        if (userName != null) {
            builder.name(userName);
        }
        String userEmail = user.getEmail();
        if (userEmail != null && userEmail.contains("@")) {
            builder.email(userEmail);
        }

        User updatedUser = builder.build();
        users.put(id, updatedUser);
        log.info("Пользователь id={} изменён", id);
        return updatedUser;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
        log.info("Пользователь id={} удалён", id);
    }

}

package ru.practicum.shareit.server.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.server.exception.NotFoundException;
import ru.practicum.shareit.server.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void extract() {
        User user = User.builder().name("TestName").email("test@test.com").build();
        User saveUser = userRepository.save(user);

        assertEquals(saveUser, userRepository.extract(saveUser.getId()));
        NotFoundException exception =
                assertThrows(NotFoundException.class, () -> userRepository.extract(saveUser.getId() + 1));
        assertEquals("Запрос на несуществующего пользователя с id = " + (saveUser.getId() + 1), exception.getMessage());
    }
}

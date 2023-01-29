package ru.practicum.shareit.user;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class UserServiceImplTestIT {
    private static final long UNKNOWN_USER_ID = Long.MAX_VALUE;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private EntityManager em;

    UserDto newUserDto = UserDto.builder().name("TestName").email("test@test.com").build();
    UserDto newUser2Dto = UserDto.builder().name("Test2Name").email("test2@test.com").build();
    String exceptionMessage = "Запрос на несуществующего пользователя с id = ";


    @Test
    void create() {
        UserDto result = userService.create(newUserDto);

        User savedUser = em
                .createQuery(
                        "select u from User u where u.name = :name and u.email = :email",
                        User.class)
                .setParameter("name", newUserDto.getName())
                .setParameter("email", newUserDto.getEmail())
                .getSingleResult();

        assertThat(savedUser.getId(), equalTo(result.getId()));
        assertThat(savedUser.getName(), equalTo(result.getName()));
        assertThat(savedUser.getEmail(), equalTo(result.getEmail()));
    }

    @Test
    void findById() {
        userService.create(newUser2Dto);
        UserDto result = userService.create(newUserDto);

        assertThat(result, equalTo(userService.findById(result.getId())));

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userService.findById(UNKNOWN_USER_ID));
        assertEquals(exceptionMessage + UNKNOWN_USER_ID, exception.getMessage());
    }

    @Test
    void findAll() {
        UserDto savedUser = userService.create(newUserDto);
        UserDto savedUser2 = userService.create(newUser2Dto);

        assertThat(userService.findAll(), Matchers.containsInAnyOrder(savedUser, savedUser2));
    }

    @Test
    void update() {
        UserDto savedUser = userService.create(newUserDto);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userService.update(UNKNOWN_USER_ID, newUser2Dto));
        assertEquals(exceptionMessage + UNKNOWN_USER_ID, exception.getMessage());

        UserDto result = userService.update(savedUser.getId(), newUser2Dto);

        User updatedUser = em
                .createQuery(
                        "select u from User u where u.id = :id",
                        User.class)
                .setParameter("id", savedUser.getId())
                .getSingleResult();
        assertThat(newUser2Dto.getName(), equalTo(updatedUser.getName()));
        assertThat(newUser2Dto.getEmail(), equalTo(updatedUser.getEmail()));

        assertThat(savedUser.getId(), equalTo(result.getId()));
        assertThat(newUser2Dto.getName(), equalTo(result.getName()));
        assertThat(newUser2Dto.getEmail(), equalTo(result.getEmail()));
    }

    @Test
    void delete() {
        UserDto savedUser = userService.create(newUserDto);

        NotFoundException exception = assertThrows(
                NotFoundException.class, () -> userService.update(UNKNOWN_USER_ID, newUser2Dto));
        assertEquals(exceptionMessage + UNKNOWN_USER_ID, exception.getMessage());

        userService.delete(savedUser.getId());

        exception = assertThrows(
                NotFoundException.class, () -> userService.update(savedUser.getId(), newUser2Dto));
        assertEquals(exceptionMessage + (savedUser.getId()), exception.getMessage());
    }
}
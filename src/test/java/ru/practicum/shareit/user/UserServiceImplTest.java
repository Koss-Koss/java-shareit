package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    long id = 1L;
    User expectedUser = User.builder().id(id).name("TestName").email("test@test.com").build();
    User oldUser = User.builder().id(id).name("TestName").email("old@test.com").build();
    List<User> expectedUserCollection = Collections.singletonList(expectedUser);

    UserDto newUserDto = UserDto.builder().name("TestName").email("test@test.com").build();
    UserDto expectedUserDto = UserDto.builder().id(id).name("TestName").email("test@test.com").build();
    Collection<UserDto> expectedUserDtoCollection = Collections.singletonList(expectedUserDto);
    String exceptionMessage = "Message";

    @Test
    void findById_whenUserFound_thenReturnedUserDto() {

        when(userRepository.extract(anyLong())).thenReturn(expectedUser);

        assertEquals(expectedUserDto, userService.findById(id));
        verify(userRepository, times(1)).extract(anyLong());
    }

    @Test
    void findById_whenUserNotFound_thenNotReturnedUserDto() {

        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findById(id));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, times(1)).extract(anyLong());
    }

    @Test
    void findAll_whenInvoked_thenReturnedUserDtoCollection() {

        when(userRepository.findAll()).thenReturn(expectedUserCollection);

        assertEquals(expectedUserDtoCollection, userService.findAll());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void create_whenUserCreated_thenReturnedUserDto() {

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        assertEquals(expectedUserDto, userService.create(newUserDto));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_whenUserUpdated_thenUpdatedOnlyAvailableFields() {

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(userRepository.extract(anyLong())).thenReturn(oldUser);

        assertEquals(expectedUserDto, userService.update(id, newUserDto));
        verify(userRepository).save(userArgumentCaptor.capture());
        User changedUser = userArgumentCaptor.getValue();
        assertEquals(expectedUser.getName(),changedUser.getName());
        assertEquals(expectedUser.getEmail(),changedUser.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void update_whenUserNotFound_NotUpdatedUser() {

        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.update(id, newUserDto));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_whenUserFound_thenDeletedUser() {

        when(userRepository.extract(anyLong())).thenReturn(expectedUser);

        userService.delete(id);

        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void delete_whenUserNotFound_thenNotDeletedUser() {

        when(userRepository.extract(anyLong())).thenThrow(new NotFoundException(exceptionMessage));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.delete(id));
        assertEquals(exceptionMessage, exception.getMessage());

        verify(userRepository, never()).deleteById(anyLong());
    }

}
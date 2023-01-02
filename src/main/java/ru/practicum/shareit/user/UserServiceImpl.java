package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto findById(long id) {
        return UserMapper.toUserDto(userRepository.extract(id));
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto create(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        userRepository.save(newUser);
        log.info("Добавлен пользователь с id = {}", newUser.getId());
        return UserMapper.toUserDto(newUser);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        User currentUser = userRepository.extract(id);
        String userName = userDto.getName();
        if (userName != null) {
            currentUser.setName(userName);
        }
        String userEmail = userDto.getEmail();
        if (userEmail != null) {
            currentUser.setEmail(userEmail);
        }
        User updatedUser = userRepository.save(currentUser);
        log.info("Обновлён пользователь с id = {}", updatedUser.getId());
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(long id) {
        userRepository.extract(id);
        userRepository.deleteById(id);
        log.info("Удалён пользователь с id = {}", id);
    }

}

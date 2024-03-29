package ru.practicum.shareit.server.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.server.user.dto.UserDto;
import ru.practicum.shareit.server.user.dto.UserMapper;
import ru.practicum.shareit.server.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        User newUser = UserMapper.toUser(userDto);
        User createdUser = userRepository.save(newUser);
        log.info("Добавлен пользователь с id = {}", createdUser.getId());
        return UserMapper.toUserDto(createdUser);
    }

    @Transactional
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

    @Transactional
    @Override
    public void delete(long id) {
        userRepository.extract(id);
        userRepository.deleteById(id);
        log.info("Удалён пользователь с id = {}", id);
    }

}

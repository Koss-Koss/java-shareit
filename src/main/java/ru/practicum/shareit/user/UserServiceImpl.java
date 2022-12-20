package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final UserValidationService userValidationService;

    @Override
    public UserDto findById(long id) {
        userValidationService.validateUserId(id);
        return UserMapper.toUserDto(userStorage.findById(id));
    }

    @Override
    public Collection<UserDto> findAll() {
        Collection<UserDto> userDtos = userStorage.findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        return userDtos;
    }

    @Override
    public UserDto create(UserDto userDto) {
        userValidationService.validateUserEmail(userDto);
        return UserMapper.toUserDto(userStorage.create(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        userValidationService.validateUserId(id);
        String userEmail = userDto.getEmail();
        if(userEmail != null && userEmail.contains("@")) {
            userValidationService.validateUserEmail(id, userDto);
        }
        return UserMapper.toUserDto(userStorage.update(id, UserMapper.toUser(userDto)));
    }

    @Override
    public void delete(long id) {
        userValidationService.validateUserId(id);
        itemStorage.deleteAllByOwnerId(id);
        userStorage.delete(id);
    }

}

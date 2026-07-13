package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.exception.NameAlreadyExistException;
import ru.practicum.main.exception.UserNotExistException;
import ru.practicum.main.mapper.UserMapper;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.UserRepository;
import ru.practicum.main.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            log.warn("Can't create user with name: {}, the name was used by another user", userDto.getName());
            throw new NameAlreadyExistException(String.format("Can't create user with name: %s, the name was used by another user",
                    userDto.getName()));
        }
        log.debug("The user with name {} was created", userDto.getName());
        return userMapper.toUserDto(userRepository.save(userMapper.toUserModel(userDto)));
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.debug("Received users");
        Pageable page = PageRequest.of(from / size, size);
        if (ids == null) {
            return userMapper.toUserDtoList(userRepository.findAll(page).toList());
        } else {
            return userMapper.toUserDtoList(userRepository.findAllById(ids));

        }
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("User with id: {} was deleted ", id);
        userRepository.deleteById(id);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotExistException(
                        String.format("User with id=%s was not found", userId)));
    }

    @Override
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }
}
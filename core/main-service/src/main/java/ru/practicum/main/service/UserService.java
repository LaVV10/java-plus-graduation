package ru.practicum.main.service;

import ru.practicum.main.dto.user.UserDto;
import ru.practicum.main.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(UserDto userModelDto);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long id);

    User getUserById(Long userId);

    List<User> getUsersByIds(List<Long> userIds);
}
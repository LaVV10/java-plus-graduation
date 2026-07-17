package ru.practicum.user.service;

import ru.practicum.common.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {
	UserDto createUser(UserDto userModelDto);

	List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

	void deleteUser(Long id);

	User getUserById(Long userId);

	List<User> getUsersByIds(List<Long> userIds);
}

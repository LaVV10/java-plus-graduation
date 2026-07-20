package ru.practicum.user.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.util.List;

/**
 * Внутреннее API user-service для межсервисного взаимодействия (Feign).
 * НЕ маршрутизируется gateway-ем наружу — префикс /internal исключён из публичных маршрутов.
 *
 * Контракт:
 *   GET /internal/users/{id}            -> UserShortDto (404 если не найден)
 *   GET /internal/users?ids=1,2,3       -> List<UserShortDto>
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping("/{id}")
	public UserShortDto getUserById(@PathVariable Long id) {
		User user = userService.getUserById(id);
		return userMapper.toUserShortDto(user);
	}

	@GetMapping
	public List<UserShortDto> getUsersByIds(@RequestParam("ids") List<Long> ids) {
		List<User> users = userService.getUsersByIds(ids);
		return userMapper.toUserShortDtoList(users);
	}
}

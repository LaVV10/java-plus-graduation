package ru.practicum.request.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.request.client.UserClient;

/**
 * Fallback для {@link UserClient}: при недоступности user-service возвращаем null,
 * что трактуется как «пользователь не найден» -> 404.
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

	@Override
	public UserShortDto getUserById(Long userId) {
		log.warn("Fallback getUserById({}): user-service недоступен, возвращаем null -> 404", userId);
		return null;
	}
}

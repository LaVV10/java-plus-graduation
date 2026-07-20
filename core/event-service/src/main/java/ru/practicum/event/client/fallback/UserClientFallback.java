package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.event.client.UserClient;

import java.util.List;

/**
 * Fallback для {@link UserClient}: при недоступности user-service пользователь = null,
 * список = пустой.
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

	@Override
	public UserShortDto getUserById(Long userId) {
		log.warn("Fallback getUserById({}): user-service недоступен, возвращаем null", userId);
		return null;
	}

	@Override
	public List<UserShortDto> getUsersByIds(List<Long> ids) {
		log.warn("Fallback getUsersByIds({}): user-service недоступен, возвращаем пустой список", ids);
		return List.of();
	}
}

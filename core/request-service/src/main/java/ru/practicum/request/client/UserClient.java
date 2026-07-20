package ru.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.request.client.fallback.UserClientFallback;

/**
 * Feign-клиент к user-service для домена request-service.
 * Используется для валидации существования пользователя (заявителя).
 *
 * Fallback @{@link UserClientFallback}: при недоступности user-service возвращается null,
 * что трактуется как «пользователь не найден» -> 404.
 */
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

	@GetMapping("/internal/users/{id}")
	UserShortDto getUserById(@PathVariable("id") Long userId);
}

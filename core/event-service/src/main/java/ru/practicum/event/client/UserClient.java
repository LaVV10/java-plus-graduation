package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.event.client.fallback.UserClientFallback;

import java.util.List;

/**
 * Feign-клиент к user-service для домена event-service.
 * Получает данные инициаторов событий.
 *
 * Fallback @{@link UserClientFallback}: при недоступности user-service пользователь = null,
 * список = пустой. Публичная выдача событий продолжает работать без initiator.
 */
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

	@GetMapping("/internal/users/{id}")
	UserShortDto getUserById(@PathVariable("id") Long userId);

	@GetMapping("/internal/users")
	List<UserShortDto> getUsersByIds(@RequestParam("ids") List<Long> ids);
}

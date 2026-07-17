package ru.practicum.feature.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.feature.client.fallback.EventClientFallback;

import java.util.List;

/**
 * Feign-клиент к event-service для домена feature-service.
 *
 * Используется:
 *   - при удалении категории (проверка, что категория не привязана к событиям);
 *   - при сборке подборок (нужно получить данные событий по их id).
 *
 * Fallback @{@link EventClientFallback}: при недоступности event-service категория считается
 * неиспользуемой (false), а события подборки — пустым списком (ТЗ: вернуть фиксированное значение).
 */
@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

	/** true, если существует хотя бы одно событие с указанной категорией. */
	@GetMapping("/internal/events/exists-by-category")
	Boolean existsByCategoryId(@RequestParam("categoryId") Long categoryId);

	/** Краткие данные событий по списку id (для подборок). */
	@GetMapping("/internal/events")
	List<EventShortDto> getEventsByIds(@RequestParam("ids") List<Long> ids);
}

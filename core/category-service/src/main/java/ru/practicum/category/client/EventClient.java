package ru.practicum.category.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.category.client.fallback.EventClientFallback;

/**
 * Feign-клиент к event-service для домена category-service.
 *
 * Используется при удалении категории — проверка, что к ней не привязано событий.
 *
 * Fallback @{@link EventClientFallback}: при недоступности event-service категория считается
 * неиспользуемой (false), иначе зависимость от event-service блокировала бы удаление категорий.
 */
@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

	/** true, если существует хотя бы одно событие с указанной категорией. */
	@GetMapping("/internal/events/exists-by-category")
	Boolean existsByCategoryId(@RequestParam("categoryId") Long categoryId);
}

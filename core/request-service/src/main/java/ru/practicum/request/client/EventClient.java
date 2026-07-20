package ru.practicum.request.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.request.client.fallback.EventClientFallback;

/**
 * Feign-клиент к event-service для домена request-service.
 * Нужен для получения снимка события при создании/модерации заявок
 * (проверка состояния, лимита участников, премодерации, автора).
 *
 * Fallback @{@link EventClientFallback}: при недоступности event-service возвращается null,
 * что трактуется слоем сервиса как «событие не найдено» -> 404 (нельзя создать заявку на неизвестное событие).
 */
@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

	/** Снимок события по id для модерации заявок. */
	@GetMapping("/internal/events/{id}/info")
	EventShortInfoDto getEventInfo(@PathVariable("id") Long eventId);
}

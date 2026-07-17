package ru.practicum.compilation.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.compilation.client.fallback.EventClientFallback;

import java.util.List;

/**
 * Feign-клиент к event-service для домена compilation-service.
 * Используется при сборке подборок — нужно получить данные событий по их id.
 *
 * Fallback @{@link EventClientFallback}: при недоступности event-service события подборки
 * возвращаются пустым списком (ТЗ: вернуть фиксированное значение).
 */
@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

	/** Краткие данные событий по списку id (для подборок). */
	@GetMapping("/internal/events")
	List<EventShortDto> getEventsByIds(@RequestParam("ids") List<Long> ids);
}

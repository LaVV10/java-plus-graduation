package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.event.client.fallback.RequestStatsClientFallback;

import java.util.List;
import java.util.Map;

/**
 * Feign-клиент к request-service для домена event-service.
 * Получает количество подтверждённых заявок по событиям (для обогащения EventFullDto/EventShortDto).
 *
 * Fallback @{@link RequestStatsClientFallback}: при недоступности request-service счётчик = 0
 * (ТЗ: «вернуть фиксированное значение 0»). Заменяет монолитный EventParticipationService.
 */
@FeignClient(name = "request-service", fallback = RequestStatsClientFallback.class)
public interface RequestStatsClient {

	@GetMapping("/internal/requests/count")
	Long countConfirmedByEvent(@RequestParam("eventId") Long eventId,
							   @RequestParam(value = "status", defaultValue = "CONFIRMED") RequestStatus status);

	@GetMapping("/internal/requests/count-batch")
	Map<Long, Long> countConfirmedByEvents(@RequestParam("eventIds") List<Long> eventIds,
										   @RequestParam(value = "status", defaultValue = "CONFIRMED") RequestStatus status);
}

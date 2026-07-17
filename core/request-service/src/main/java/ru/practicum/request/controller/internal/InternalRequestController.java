package ru.practicum.request.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.request.repository.RequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Внутреннее API request-service для межсервисного взаимодействия (Feign) с event-service.
 * НЕ маршрутизируется gateway-ем наружу.
 *
 * Контракт (подсчёт подтверждённых заявок для публичной выдачи событий):
 *   GET /internal/requests/count?eventId=1&status=CONFIRMED            -> Long
 *   GET /internal/requests/count-batch?eventIds=1,2&status=CONFIRMED   -> Map<Long, Long> (eventId -> count)
 *
 * Заменяет монолитный EventParticipationService: фиксированное число запросов (N+1 устранён).
 */
@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {

	private final RequestRepository requestRepository;

	@GetMapping("/count")
	public Long countByEvent(@RequestParam("eventId") Long eventId,
							 @RequestParam(value = "status", defaultValue = "CONFIRMED") RequestStatus status) {
		Integer count = requestRepository.countByEventIdAndStatus(eventId, status);
		return count == null ? 0L : count.longValue();
	}

	@GetMapping("/count-batch")
	public Map<Long, Long> countByEvents(@RequestParam("eventIds") List<Long> eventIds,
										 @RequestParam(value = "status", defaultValue = "CONFIRMED") RequestStatus status) {
		Map<Long, Long> result = new HashMap<>();
		if (eventIds == null || eventIds.isEmpty()) {
			return result;
		}
		for (Long eventId : eventIds) {
			Integer count = requestRepository.countByEventIdAndStatus(eventId, status);
			result.put(eventId, count == null ? 0L : count.longValue());
		}
		return result;
	}
}

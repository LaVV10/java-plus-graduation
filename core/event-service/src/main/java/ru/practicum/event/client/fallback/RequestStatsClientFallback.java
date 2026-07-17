package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.event.client.RequestStatsClient;

import java.util.List;
import java.util.Map;

/**
 * Fallback для {@link RequestStatsClient}: при недоступности request-service счётчик
 * подтверждённых заявок = 0. Публичная выдача событий не падает (ТЗ: «вернуть фиксированное значение 0»).
 */
@Slf4j
@Component
public class RequestStatsClientFallback implements RequestStatsClient {

	@Override
	public Long countConfirmedByEvent(Long eventId, RequestStatus status) {
		log.warn("Fallback countConfirmedByEvent({}): request-service недоступен, возвращаем 0", eventId);
		return 0L;
	}

	@Override
	public Map<Long, Long> countConfirmedByEvents(List<Long> eventIds, RequestStatus status) {
		log.warn("Fallback countConfirmedByEvents({}): request-service недоступен, возвращаем пустую map", eventIds);
		return Map.of();
	}
}

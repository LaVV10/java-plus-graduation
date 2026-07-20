package ru.practicum.compilation.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.compilation.client.EventClient;

import java.util.List;

/**
 * Fallback для {@link EventClient} при недоступности event-service.
 *
 * Политика (по ТЗ «вернуть фиксированное значение»):
 *   getEventsByIds -> пустой список (подборка вернётся без раскрытия событий).
 */
@Slf4j
@Component
public class EventClientFallback implements EventClient {

	@Override
	public List<EventShortDto> getEventsByIds(List<Long> ids) {
		log.warn("Fallback getEventsByIds({}): event-service недоступен, возвращаем пустой список", ids);
		return List.of();
	}
}

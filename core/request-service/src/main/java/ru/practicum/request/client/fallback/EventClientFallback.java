package ru.practicum.request.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.request.client.EventClient;

/**
 * Fallback для {@link EventClient}: при недоступности event-service возвращаем null,
 * что трактуется сервисным слоем как «событие не найдено». Создать заявку в таком случае
 * нельзя (404) — это критичная зависимость, фиктивные значения здесь недопустимы.
 */
@Slf4j
@Component
public class EventClientFallback implements EventClient {

	@Override
	public EventShortInfoDto getEventInfo(Long eventId) {
		log.warn("Fallback getEventInfo({}): event-service недоступен, возвращаем null -> 404", eventId);
		return null;
	}
}

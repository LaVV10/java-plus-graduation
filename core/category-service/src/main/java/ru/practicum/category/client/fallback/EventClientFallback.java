package ru.practicum.category.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.category.client.EventClient;

/**
 * Fallback для {@link EventClient} при недоступности event-service.
 *
 * Политика (по ТЗ «вернуть фиксированное значение»):
 *   existsByCategoryId -> false (считаем категорию неиспользуемой, чтобы дать удалить её;
 *   иначе зависимость от event-service блокировала бы удаление категорий).
 */
@Slf4j
@Component
public class EventClientFallback implements EventClient {

	@Override
	public Boolean existsByCategoryId(Long categoryId) {
		log.warn("Fallback existsByCategoryId({}): event-service недоступен, возвращаем false", categoryId);
		return Boolean.FALSE;
	}
}

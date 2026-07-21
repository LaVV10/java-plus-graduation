package ru.practicum.event.controller.publicAccess;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.event.enumeration.SortValue;
import ru.practicum.event.service.EventService;

import java.util.List;

/**
 * Публичные эндпоинты мероприятий.
 *
 * <p>На Этапе 3-2:
 * <ul>
 *   <li>{@code GET /events/{id}} — фиксирует просмотр через Collector
 *       (идентификатор пользователя из заголовка {@code X-EWM-USER-ID});</li>
 *   <li>{@code GET /events} — больше не отправляет информацию о просмотре;</li>
 *   <li>{@code GET /events/recommendations} — персональные рекомендации;</li>
 *   <li>{@code PUT /events/{eventId}/like} — лайк посещённого мероприятия.</li>
 * </ul>
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
	private static final String USER_ID_HEADER = "X-EWM-USER-ID";

	private final EventService eventService;

	@GetMapping
	public List<EventFullDto> getEventsWithParamsByUser(
			@RequestParam(name = "text", required = false) String text,
			@RequestParam(name = "users", required = false) List<Long> users,
			@RequestParam(name = "categories", required = false) List<Long> categories,
			@RequestParam(name = "paid", required = false) Boolean paid,
			@RequestParam(name = "rangeStart", required = false) String rangeStart,
			@RequestParam(name = "rangeEnd", required = false) String rangeEnd,
			@RequestParam(name = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
			@RequestParam(name = "sort", required = false) SortValue sort,
			@RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
			@RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
			@RequestParam(name = "states", required = false) List<String> states) {

		return eventService.getEventsWithParamsByUser(text, users, categories, paid, rangeStart,
				rangeEnd, onlyAvailable, sort, from, size, states);
	}

	@GetMapping("/recommendations")
	public List<EventShortDto> getRecommendations(
			@RequestHeader(USER_ID_HEADER) Long userId,
			@RequestParam(name = "maxResults", required = false, defaultValue = "10") Integer maxResults) {
		return eventService.getRecommendations(userId, maxResults);
	}

	@GetMapping("/{id}")
	public EventFullDto getEvent(@PathVariable Long id,
								 @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
		return eventService.getEvent(id, userId);
	}

	@PutMapping("/{eventId}/like")
	public void likeEvent(@PathVariable Long eventId,
						  @RequestHeader(USER_ID_HEADER) Long userId) {
		eventService.likeEvent(userId, eventId);
	}
}

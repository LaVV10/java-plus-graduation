package ru.practicum.event.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.event.service.EventService;

import java.util.List;

/**
 * Внутреннее API event-service для межсервисного взаимодействия (Feign).
 * НЕ маршрутизируется gateway-ем наружу.
 *
 * Контракт:
 *   GET /internal/events/{id}/info                  -> EventShortInfoDto (снимок для request-service)
 *   GET /internal/events/exists-by-category?categoryId= -> Boolean (для category-service deleteCategory)
 *   GET /internal/events?ids=1,2                    -> List<EventShortDto> (для compilation-service compilations)
 */
@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

	private final EventService eventService;

	@GetMapping("/{id}/info")
	public EventShortInfoDto getEventInfo(@PathVariable Long id) {
		return eventService.getEventInfo(id);
	}

	@GetMapping("/exists-by-category")
	public Boolean existsByCategoryId(@RequestParam("categoryId") Long categoryId) {
		return eventService.existsByCategoryId(categoryId);
	}

	@GetMapping
	public List<EventShortDto> getEventsByIds(@RequestParam("ids") List<Long> ids) {
		return eventService.getEventShortDtosByIds(ids);
	}
}

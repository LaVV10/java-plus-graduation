package ru.practicum.event.controller.privateAccess;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.service.EventService;

import java.util.List;

/**
 * Private API событий инициатора. Содержит ТОЛЬКО event-эндпоинты.
 * Заявки в рамках события (/users/{userId}/events/{eventId}/requests) перенесены в request-service.
 */
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
	private final EventService eventService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public EventFullDto createEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto) {
		return eventService.createEvent(userId, newEventDto);
	}

	@GetMapping
	public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
											   @RequestParam(name = "from", defaultValue = "0", required = false) Integer from,
											   @RequestParam(name = "size", defaultValue = "10", required = false) Integer size) {
		return eventService.getEvents(userId, from, size);
	}

	@PatchMapping("/{eventId}")
	public EventFullDto updateEventByUser(@PathVariable Long userId,
										  @PathVariable Long eventId,
										  @RequestBody @Valid UpdateEventUserDto updateEventUserDto) {
		return eventService.updateEventByUser(userId, eventId, updateEventUserDto);
	}

	@GetMapping("/{eventId}")
	public EventFullDto getEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
		return eventService.getEventByUser(userId, eventId);
	}
}

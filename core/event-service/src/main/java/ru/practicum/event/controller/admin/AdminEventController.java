package ru.practicum.event.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.Constants;
import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
	private final EventService eventService;

	@PatchMapping("/{eventId}")
	public EventFullDto updateEvent(@PathVariable Long eventId,
									@RequestBody @Valid UpdateEventAdminDto updateEventAdminDto) {
		return eventService.updateEvent(eventId, updateEventAdminDto);

	}

	@PatchMapping("/{eventId}/publish")
	public EventFullDto publishEvent(@PathVariable Long eventId) {
		return eventService.publishEvent(eventId);
	}

	@PatchMapping("/{eventId}/reject")
	public EventFullDto rejectEvent(@PathVariable Long eventId) {
		return eventService.rejectEvent(eventId);
	}

	@GetMapping("/pending")
	public List<EventFullDto> getPendingEvents(@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
											   @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero Integer size) {
		return eventService.getPendingEvents(from, size);
	}

	@GetMapping
	public List<EventFullDto> getEvents(@RequestParam(name = "users", required = false) List<Long> users,
										@RequestParam(name = "states", required = false) EventState states,
										@RequestParam(name = "categories", required = false) List<Long> categoriesId,
										@RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
										LocalDateTime rangeStart,
										@RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT) LocalDateTime rangeEnd,
										@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
										@RequestParam(name = "size", defaultValue = "10") @PositiveOrZero Integer size) {
		return eventService.getEventsWithParamsByAdmin(users, states, categoriesId, rangeStart, rangeEnd, from, size);
	}
}

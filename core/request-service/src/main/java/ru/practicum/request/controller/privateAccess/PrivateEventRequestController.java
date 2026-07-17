package ru.practicum.request.controller.privateAccess;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

/**
 * Заявки в рамках события (Private API инициатора события).
 * В монолите находились в PrivateEventController; перенесены в request-service
 * (домен заявок). Маршрутизируются gateway-ем отдельно от event-эндпоинтов.
 *
 *   GET   /users/{userId}/events/{eventId}/requests
 *   PATCH /users/{userId}/events/{eventId}/requests
 */
@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class PrivateEventRequestController {

	private final RequestService requestService;

	@GetMapping
	public List<RequestDto> getEventParticipants(@PathVariable Long userId,
												 @PathVariable Long eventId) {
		return requestService.getRequestsByOwnerOfEvent(userId, eventId);
	}

	@PatchMapping
	public RequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
														 @PathVariable Long eventId,
														 @RequestBody RequestStatusUpdateDto requestStatusUpdateDto) {
		return requestService.updateRequests(userId, eventId, requestStatusUpdateDto);
	}
}

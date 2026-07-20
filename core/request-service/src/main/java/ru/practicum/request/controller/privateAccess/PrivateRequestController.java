package ru.practicum.request.controller.privateAccess;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

/**
 * Заявки текущего пользователя (Private API).
 * Маршрутизируется gateway-ем в request-service.
 */
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {

	private final RequestService requestService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RequestDto createRequest(@PathVariable Long userId,
									@RequestParam Long eventId) {
		return requestService.createRequest(userId, eventId);
	}

	@GetMapping
	public List<RequestDto> getCurrentUserRequests(@PathVariable Long userId) {
		return requestService.getCurrentUserRequests(userId);
	}

	@PatchMapping("/{requestId}/cancel")
	public RequestDto cancelRequests(@PathVariable Long userId,
									 @PathVariable Long requestId) {
		return requestService.cancelRequests(userId, requestId);
	}
}

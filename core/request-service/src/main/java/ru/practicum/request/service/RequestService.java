package ru.practicum.request.service;

import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResult;

import java.util.List;

public interface RequestService {
	RequestDto createRequest(Long userId, Long eventId);

	List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId);

	RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto);

	List<RequestDto> getCurrentUserRequests(Long userId);

	RequestDto cancelRequests(Long userId, Long requestId);
}

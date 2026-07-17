package ru.practicum.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.enums.EventState;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.request.client.EventClient;
import ru.practicum.request.client.UserClient;
import ru.practicum.request.dto.RequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.dto.RequestStatusUpdateResult;
import ru.practicum.request.dto.RequestStatusToUpdate;
import ru.practicum.request.exception.EventIsNotPublishedException;
import ru.practicum.request.exception.EventNotExistException;
import ru.practicum.request.exception.ParticipantLimitException;
import ru.practicum.request.exception.RequestAlreadyExistException;
import ru.practicum.request.exception.RequestAlreadyConfirmedException;
import ru.practicum.request.exception.RequestNotExistException;
import ru.practicum.request.exception.UserNotExistException;
import ru.practicum.request.exception.WrongDataException;
import ru.practicum.request.exception.WrongUserException;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
	private final RequestRepository requestRepository;
	private final EventClient eventClient;
	private final UserClient userClient;
	private final RequestMapper requestMapper;

	@Override
	public List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) {
		List<Request> requests = getParticipationRequests(userId, eventId);
		return requestMapper.toRequestDtoList(requests);
	}

	@Override
	@Transactional
	public RequestDto createRequest(Long userId, Long eventId) {
		if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
			throw new RequestAlreadyExistException("Request already exists");
		}

		EventShortInfoDto event = requireEventInfo(eventId);
		requireUser(userId);

		if (event.getInitiatorId() != null && event.getInitiatorId().equals(userId)) {
			throw new WrongUserException("Can't create request by initiator");
		}

		if (event.getState() == EventState.PENDING) {
			throw new EventIsNotPublishedException("Event is not published yet");
		}

		Integer confirmedRequests = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);

		if (!event.getRequestModeration() && confirmedRequests >= event.getParticipantLimit()) {
			throw new ParticipantLimitException("Member limit exceeded ");
		}

		Request request = new Request();
		request.setCreated(LocalDateTime.now());
		request.setEventId(event.getId());
		request.setRequesterId(userId);

		if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == null
				|| event.getParticipantLimit() == 0) {
			request.setStatus(RequestStatus.CONFIRMED);
		} else {
			request.setStatus(RequestStatus.PENDING);
		}

		return requestMapper.toRequestDto(requestRepository.save(request));
	}

	@Transactional
	@Override
	public RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
		EventShortInfoDto event = requireEventInfo(eventId);
		RequestStatusUpdateResult result = new RequestStatusUpdateResult();

		if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == null
				|| event.getParticipantLimit() == 0) {
			throw new WrongDataException("Нет доступа или количество заявок равно 0");
		}

		List<Request> requests = getParticipationRequests(userId, eventId);
		List<Request> requestsToUpdate = requests.stream()
				.filter(x -> requestStatusUpdateDto.getRequestIds().contains(x.getId()))
				.collect(Collectors.toList());

		if (requestsToUpdate.stream().anyMatch(x -> x.getStatus().equals(RequestStatus.CONFIRMED)
				&& requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED))) {
			throw new RequestAlreadyConfirmedException("request already confirmed");
		}

		if (requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED) + requestsToUpdate.size() > event.getParticipantLimit()
				&& requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
			throw new ParticipantLimitException("exceeding the limit of participants");
		}

		for (Request x : requestsToUpdate) {
			x.setStatus(RequestStatus.valueOf(requestStatusUpdateDto.getStatus().toString()));
		}

		requestRepository.saveAll(requestsToUpdate);

		if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.CONFIRMED)) {
			result.setConfirmedRequests(requestMapper.toRequestDtoList(requestsToUpdate));
		}

		if (requestStatusUpdateDto.getStatus().equals(RequestStatusToUpdate.REJECTED)) {
			result.setRejectedRequests(requestMapper.toRequestDtoList(requestsToUpdate));
		}

		return result;
	}

	@Override
	public List<RequestDto> getCurrentUserRequests(Long userId) {
		requireUser(userId);
		return requestMapper.toRequestDtoList(requestRepository.findAllByRequesterId(userId));
	}

	@Override
	public RequestDto cancelRequests(Long userId, Long requestId) {
		Request request = requestRepository.findByRequesterIdAndId(userId, requestId)
				.orElseThrow(() -> new RequestNotExistException(String.format("Request with id=%s was not found", requestId)));
		request.setStatus(RequestStatus.CANCELED);
		return requestMapper.toRequestDto(requestRepository.save(request));
	}

	private List<Request> getParticipationRequests(Long userId, Long eventId) {
		EventShortInfoDto event = requireEventInfo(eventId);
		requireUser(userId);

		if (event.getInitiatorId() == null || !event.getInitiatorId().equals(userId)) {
			throw new WrongDataException("Пользователь " + userId + " не инициатор события " + eventId);
		}

		return requestRepository.findByEventId(eventId);
	}

	private EventShortInfoDto requireEventInfo(Long eventId) {
		EventShortInfoDto event = eventClient.getEventInfo(eventId);
		if (event == null) {
			throw new EventNotExistException(String.format("Event with id=%s was not found", eventId));
		}
		return event;
	}

	private void requireUser(Long userId) {
		UserShortDto user = userClient.getUserById(userId);
		if (user == null) {
			throw new UserNotExistException(String.format("User with id=%s was not found", userId));
		}
	}
}

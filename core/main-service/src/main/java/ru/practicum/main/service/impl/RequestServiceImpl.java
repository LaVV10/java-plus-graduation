package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.request.RequestDto;
import ru.practicum.main.dto.request.RequestStatusUpdateDto;
import ru.practicum.main.dto.request.RequestStatusUpdateResult;
import ru.practicum.main.enumeration.EventState;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.enumeration.RequestStatusToUpdate;
import ru.practicum.main.exception.*;
import ru.practicum.main.mapper.RequestMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;
import ru.practicum.main.model.User;
import ru.practicum.main.repository.RequestRepository;
import ru.practicum.main.service.EventService;
import ru.practicum.main.service.RequestService;
import ru.practicum.main.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserService userService;
    private final RequestMapper requestMapper;

    @Override
    public List<RequestDto> getRequestsByOwnerOfEvent(Long userId, Long eventId) {
        List<Request> requests = getParticipationRequests(userId, eventId);
        return requestMapper.toRequestDtoList(requests);
    }

    @Override
    public RequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new RequestAlreadyExistException("Request already exists");
        }

        Event event = eventService.getEventById(eventId);
        User user = userService.getUserById(userId);

        if (event.getInitiator().getId().equals(userId)) {
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
        request.setEvent(event);
        request.setRequester(user);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional
    @Override
    public RequestStatusUpdateResult updateRequests(Long userId, Long eventId, RequestStatusUpdateDto requestStatusUpdateDto) {
        Event event = eventService.getEventById(eventId);
        RequestStatusUpdateResult result = new RequestStatusUpdateResult();

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
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
        userService.getUserById(userId);
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
        Event event = eventService.getEventById(eventId);
        User user = userService.getUserById(userId);

        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new WrongDataException("Пользователь " + userId + " не инициатор события " + eventId);
        }

        return requestRepository.findByEventInitiatorId(userId);
    }
}
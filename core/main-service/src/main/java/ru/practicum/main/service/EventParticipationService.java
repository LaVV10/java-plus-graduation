package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;
import ru.practicum.main.repository.RequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventParticipationService {

    private final RequestRepository requestRepository;

    public Long getConfirmedRequestsCountForEvent(Long eventId) {
        Integer count = requestRepository.countByEventIdAndStatus(
                eventId, RequestStatus.CONFIRMED);

        return count != null ? count.longValue() : 0L;
    }

    public Map<Long, Long> getConfirmedRequestsCountForEvents(List<Event> events) {

        List<Event> publishedEvents = events.stream()
                .filter(e -> e.getPublishedOn() != null)
                .toList();

        if (publishedEvents.isEmpty()) {
            return new HashMap<>();
        }

        List<Request> confirmedRequests =
                requestRepository.findAllByEventInAndStatus(
                        publishedEvents,
                        RequestStatus.CONFIRMED
                );

        return confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEvent().getId(),
                        Collectors.counting()
                ));
    }
}

package ru.practicum.main.service;

import ru.practicum.main.dto.event.*;
import ru.practicum.main.enumeration.EventState;
import ru.practicum.main.enumeration.SortValue;
import ru.practicum.main.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminDto updateEventAdminDto);

    EventFullDto publishEvent(Long eventId);

    EventFullDto rejectEvent(Long eventId);

    List<EventFullDto> getPendingEvents(Integer from, Integer size);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto updateEventUserDto);

    EventFullDto getEventByUser(Long userId, Long eventId);

    List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> users, List<Long> categories,
                                                 Boolean paid, String rangeStart, String rangeEnd,
                                                 Boolean onlyAvailable, SortValue sort, Integer from, Integer size,
                                                 String ip, String uri, List<String> states);

    EventFullDto getEvent(Long id, String ip, String uri);

    boolean existsByCategoryId(Long categoryId);

    List<Event> getEventsByIds(List<Long> eventIds);

    Event getEventById(Long eventId);

    void setView(List<EventFullDto> events);
}
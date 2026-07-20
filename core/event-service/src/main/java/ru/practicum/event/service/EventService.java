package ru.practicum.event.service;

import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.enumeration.SortValue;

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

	List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
												  LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

	List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> users, List<Long> categories,
												 Boolean paid, String rangeStart, String rangeEnd,
												 Boolean onlyAvailable, SortValue sort, Integer from,
												 Integer size, String ip, String uri, List<String> states);

	EventFullDto getEvent(Long id, String ip, String uri);

	boolean existsByCategoryId(Long categoryId);

	/** Внутренний доступ для request-service: снимок события. */
	EventShortInfoDto getEventInfo(Long eventId);

	/** Внутренний доступ для compilation-service: краткие данные событий по списку id (для подборок). */
	List<EventShortDto> getEventShortDtosByIds(List<Long> eventIds);
}

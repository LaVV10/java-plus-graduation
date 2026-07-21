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
												 Integer size, List<String> states);

	/**
	 * Публичное получение события по id. Если передан {@code userId} (заголовок
	 * {@code X-EWM-USER-ID}), фиксирует просмотр через Collector (ACTION_VIEW).
	 */
	EventFullDto getEvent(Long id, Long userId);

	boolean existsByCategoryId(Long categoryId);

	/** Внутренний доступ для request-service: снимок события. */
	EventShortInfoDto getEventInfo(Long eventId);

	/** Внутренний доступ для compilation-service: краткие данные событий по списку id (для подборок). */
	List<EventShortDto> getEventShortDtosByIds(List<Long> eventIds);

	/**
	 * Рекомендации мероприятий для пользователя на основе предсказания оценки (через Analyzer).
	 *
	 * @param userId     идентификатор пользователя (из заголовка X-EWM-USER-ID)
	 * @param maxResults ограничение количества
	 */
	List<EventShortDto> getRecommendations(Long userId, Integer maxResults);

	/**
	 * Лайк мероприятия пользователем. По ТЗ пользователь может лайкать только посещённые
	 * им мероприятия (есть запись о просмотре), иначе {@code 400 BAD REQUEST}.
	 * Отправляет {@code ACTION_LIKE} в Collector.
	 */
	void likeEvent(Long userId, Long eventId);
}

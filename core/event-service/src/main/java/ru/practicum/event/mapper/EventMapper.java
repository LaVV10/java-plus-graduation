package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.common.Constants;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер событий. Категория и инициатор больше не являются JPA-связями Event: они живут
 * в других сервисах. Поэтому CategoryDto/UserShortDto передаются параметрами при сборке DTO —
 * их сервисный слой получает через Feign и прокидывает в маппер.
 *
 * confirmedRequests/rating всегда инициализируются нулями; реальные значения проставляет сервис.
 */
@Component
public class EventMapper {

	private final LocationMapper locationMapper;

	public EventMapper(LocationMapper locationMapper) {
		this.locationMapper = locationMapper;
	}

	public EventFullDto toEventFullDto(Event event, CategoryDto category, UserShortDto initiator) {
		if (event == null) {
			return null;
		}

		EventFullDto dto = new EventFullDto();
		dto.setId(event.getId());
		dto.setTitle(event.getTitle());
		dto.setAnnotation(event.getAnnotation());
		dto.setDescription(event.getDescription());
		dto.setEventDate(event.getEventDate());
		dto.setPaid(event.getPaid());
		dto.setParticipantLimit((long) event.getParticipantLimit());
		dto.setRequestModeration(event.getRequestModeration());
		dto.setState(event.getState());
		dto.setPublishedOn(event.getPublishedOn());

		if (event.getCreatedOn() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);
			dto.setCreatedOn(event.getCreatedOn().format(formatter));
		}

		dto.setCategory(category);
		dto.setInitiator(initiator);
		dto.setLocation(locationMapper.toLocationDto(event.getLocation()));

		dto.setRating(0.0);
		dto.setConfirmedRequests(0L);

		return dto;
	}

	public Event toEventModel(NewEventDto dto) {
		if (dto == null) {
			return null;
		}

		Event event = new Event();
		event.setTitle(dto.getTitle());
		event.setAnnotation(dto.getAnnotation());
		event.setDescription(dto.getDescription());
		event.setEventDate(dto.getEventDate());
		event.setLocation(locationMapper.toLocationModel(dto.getLocation()));
		event.setPaid(dto.isPaid());
		event.setParticipantLimit(dto.getParticipantLimit());
		event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

		event.setState(EventState.PENDING);
		event.setCreatedOn(LocalDateTime.now());

		if (dto.getCategory() != null) {
			event.setCategoryId(dto.getCategory());
		}

		return event;
	}

	public EventShortDto toEventShortDto(Event event, CategoryDto category, UserShortDto initiator) {
		if (event == null) {
			return null;
		}

		EventShortDto dto = new EventShortDto();
		dto.setId(event.getId());
		dto.setTitle(event.getTitle());
		dto.setAnnotation(event.getAnnotation());
		dto.setEventDate(event.getEventDate());
		dto.setPaid(event.getPaid());

		dto.setCategory(category);
		dto.setInitiator(initiator);

		dto.setRating(0.0);
		dto.setConfirmedRequests(0L);

		return dto;
	}

	public List<EventFullDto> toEventFullDtoList(List<Event> events,
												 java.util.Map<Long, CategoryDto> categoryByEvent,
												 java.util.Map<Long, UserShortDto> initiatorByEvent) {
		if (events == null) {
			return null;
		}
		return events.stream()
				.map(e -> toEventFullDto(e,
						categoryByEvent == null ? null : categoryByEvent.get(e.getCategoryId()),
						initiatorByEvent == null ? null : initiatorByEvent.get(e.getInitiatorId())))
				.collect(Collectors.toList());
	}

	public List<EventShortDto> toEventShortDtoList(List<Event> events,
												   java.util.Map<Long, CategoryDto> categoryByEvent,
												   java.util.Map<Long, UserShortDto> initiatorByEvent) {
		if (events == null) {
			return new java.util.ArrayList<>();
		}
		return events.stream()
				.map(e -> toEventShortDto(e,
						categoryByEvent == null ? null : categoryByEvent.get(e.getCategoryId()),
						initiatorByEvent == null ? null : initiatorByEvent.get(e.getInitiatorId())))
				.collect(Collectors.toList());
	}
}

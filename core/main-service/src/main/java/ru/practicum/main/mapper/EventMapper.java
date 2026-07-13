package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.EventShortDto;
import ru.practicum.main.dto.event.NewEventDto;
import ru.practicum.main.enumeration.EventState;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public EventMapper(CategoryMapper categoryMapper,
                       UserMapper userMapper,
                       LocationMapper locationMapper) {
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.locationMapper = locationMapper;
    }

    public EventFullDto toEventFullDto(Event event) {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ConfigUtil.DATE);
            dto.setCreatedOn(event.getCreatedOn().format(formatter));
        }

        if (event.getCategory() != null) {
            dto.setCategory(categoryMapper.toCategoryDto(event.getCategory()));
        }

        if (event.getInitiator() != null) {
            dto.setInitiator(userMapper.toUserShortDto(event.getInitiator()));
        }

        if (event.getLocation() != null) {
            dto.setLocation(locationMapper.toLocationDto(event.getLocation()));
        }

        dto.setViews(0L);
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
        event.setLocation(dto.getLocation());
        event.setPaid(dto.isPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration() != null ? dto.getRequestModeration() : true);

        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        if (dto.getCategory() != null) {
            Category category = new Category();
            category.setId(dto.getCategory());
            event.setCategory(category);
        }

        return event;
    }

    public EventShortDto toEventShortDto(Event event) {
        if (event == null) {
            return null;
        }

        EventShortDto dto = new EventShortDto();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setAnnotation(event.getAnnotation());
        dto.setEventDate(event.getEventDate());
        dto.setPaid(event.getPaid());

        if (event.getCategory() != null) {
            dto.setCategory(categoryMapper.toCategoryDto(event.getCategory()));
        }

        if (event.getInitiator() != null) {
            dto.setInitiator(userMapper.toUserShortDto(event.getInitiator()));
        }

        dto.setViews(0L);
        dto.setConfirmedRequests(0L);

        return dto;
    }

    public List<EventShortDto> toEventShortDtoList(List<Event> events) {
        return events == null ? new java.util.ArrayList<>() :
                events.stream().map(this::toEventShortDto).collect(Collectors.toList());
    }

    public List<EventFullDto> toEventFullDtoList(List<Event> events) {
        return events == null ? null :
                events.stream().map(this::toEventFullDto).collect(Collectors.toList());
    }
}
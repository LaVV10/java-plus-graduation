package ru.practicum.main.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.dto.event.UpdateEventAdminDto;
import ru.practicum.main.dto.event.UpdateEventUserDto;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.Event;
import ru.practicum.main.service.CategoryService;
import ru.practicum.main.service.EventParticipationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventUpdater {
    private final CategoryService categoryService;
    private final EventMapper eventMapper;
    private final EventParticipationService eventParticipationService;

    public void applyAdminUpdate(Event event, UpdateEventAdminDto dto) {
        Optional.ofNullable(dto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(dto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(dto.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(dto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(dto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(dto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(dto.getParticipantLimit())
                .ifPresent(limit -> event.setParticipantLimit(limit.intValue()));
        Optional.ofNullable(dto.getCategory())
                .map(categoryService::getCategoryModelById)
                .ifPresent(event::setCategory);
    }

    public void applyUserUpdate(Event event, UpdateEventUserDto dto) {
        Optional.ofNullable(dto.getAnnotation()).ifPresent(event::setAnnotation);
        Optional.ofNullable(dto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(dto.getLocation()).ifPresent(event::setLocation);
        Optional.ofNullable(dto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(dto.getTitle()).ifPresent(event::setTitle);
        Optional.ofNullable(dto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(dto.getParticipantLimit())
                .ifPresent(limit -> event.setParticipantLimit(limit.intValue()));
        Optional.ofNullable(dto.getCategory())
                .map(categoryService::getCategoryModelById)
                .ifPresent(event::setCategory);
    }

    public List<EventFullDto> processEvents(List<Event> events) {
        List<EventFullDto> dtos = eventMapper.toEventFullDtoList(events);
        Map<Long, Long> confirmedRequests = eventParticipationService.getConfirmedRequestsCountForEvents(events);

        dtos.forEach(dto -> dto.setConfirmedRequests(
                confirmedRequests.getOrDefault(dto.getId(), 0L)
        ));

        return dtos;
    }
}
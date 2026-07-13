package ru.practicum.main.controller.publicAccess;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.event.EventFullDto;
import ru.practicum.main.enumeration.SortValue;
import ru.practicum.main.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsWithParamsByUser(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "users", required = false) List<Long> users,
            @RequestParam(name = "categories", required = false) List<Long> categories,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(name = "sort", required = false) SortValue sort,
            @RequestParam(name = "from", required = false, defaultValue = "0") Integer from,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "states", required = false) List<String> states,
            HttpServletRequest request) {

        return eventService.getEventsWithParamsByUser(text, users, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sort, from, size, request.getRemoteAddr(),
                request.getRequestURI(), states);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getEvent(id, request.getRemoteAddr(), request.getRequestURI());
    }
}
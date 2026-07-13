package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.compilation.CompilationDto;
import ru.practicum.main.model.Compilation;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompilationMapper {

    private final EventMapper eventMapper;

    public CompilationMapper(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public CompilationDto mapToCompilationDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            dto.setEvents(eventMapper.toEventShortDtoList(
                    new java.util.ArrayList<>(compilation.getEvents())
            ));
        } else {
            dto.setEvents(new java.util.ArrayList<>());
        }

        return dto;
    }

    public List<CompilationDto> mapToListCompilationDto(List<Compilation> compilations) {
        if (compilations == null) {
            return null;
        }

        return compilations.stream()
                .map(this::mapToCompilationDto)
                .collect(Collectors.toList());
    }
}
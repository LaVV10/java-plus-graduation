package ru.practicum.feature.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.feature.dto.CompilationDto;
import ru.practicum.feature.model.Compilation;

import java.util.List;

@Component
public class CompilationMapper {

	/**
	 * Собирает CompilationDto. События передаются уже раскрытыми (EventShortDto получены через
	 * Feign у event-service) — отдельным параметром, т.к. в Compilation хранятся только id.
	 */
	public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
		if (compilation == null) {
			return null;
		}

		CompilationDto dto = new CompilationDto();
		dto.setId(compilation.getId());
		dto.setTitle(compilation.getTitle());
		dto.setPinned(compilation.getPinned());
		dto.setEvents(events == null ? List.of() : events);
		return dto;
	}
}

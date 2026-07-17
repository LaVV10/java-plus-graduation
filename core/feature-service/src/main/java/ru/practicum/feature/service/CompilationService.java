package ru.practicum.feature.service;

import ru.practicum.feature.dto.CompilationDto;
import ru.practicum.feature.dto.NewCompilationDto;
import ru.practicum.feature.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
	CompilationDto createCompilation(NewCompilationDto newCompilationDto);

	CompilationDto getCompilation(Long compId);

	List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

	void deleteCompilation(Long compId);

	CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);
}

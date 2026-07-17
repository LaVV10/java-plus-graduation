package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
	CompilationDto createCompilation(NewCompilationDto newCompilationDto);

	CompilationDto getCompilation(Long compId);

	List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

	void deleteCompilation(Long compId);

	CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);
}

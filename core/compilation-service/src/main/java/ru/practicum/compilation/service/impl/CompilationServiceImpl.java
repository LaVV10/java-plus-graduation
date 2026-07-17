package ru.practicum.compilation.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.compilation.client.EventClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.exception.CompilationNotExistException;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.compilation.service.CompilationService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
	private final EventClient eventClient;
	private final EntityManager entityManager;
	private final CompilationRepository compilationRepository;
	private final CompilationMapper mapper;

	@Override
	@Transactional
	public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
		Compilation compilation = new Compilation();
		if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
			compilation.setEventIds(new ArrayList<>(newCompilationDto.getEvents()));
		}
		compilation.setPinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false);
		compilation.setTitle(newCompilationDto.getTitle());

		Compilation savedCompilation = compilationRepository.save(compilation);
		log.debug("Compilation is created");
		return toDto(savedCompilation);
	}

	@Override
	public CompilationDto getCompilation(Long compId) {
		Compilation compilation = compilationRepository.findById(compId)
				.orElseThrow(() -> new CompilationNotExistException("Compilation doesn't exist"));
		return toDto(compilation);
	}

	@Override
	public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Compilation> query = builder.createQuery(Compilation.class);

		Root<Compilation> root = query.from(Compilation.class);
		Predicate criteria = builder.conjunction();

		if (pinned != null) {
			Predicate isPinned = pinned ?
					builder.isTrue(root.get("pinned")) :
					builder.isFalse(root.get("pinned"));
			criteria = builder.and(criteria, isPinned);
		}

		query.select(root).where(criteria);
		List<Compilation> compilations = entityManager.createQuery(query)
				.setFirstResult(from)
				.setMaxResults(size)
				.getResultList();

		// Все id событий из всех подборок — одним batch-запросом в event-service (фиксированное число запросов, не N+1).
		List<Long> allEventIds = compilations.stream()
				.flatMap(c -> c.getEventIds().stream())
				.distinct()
				.toList();
		List<EventShortDto> allEvents = allEventIds.isEmpty() ? List.of() : eventClient.getEventsByIds(allEventIds);

		return compilations.stream()
				.map(c -> toDto(c, eventsFor(c, allEvents)))
				.toList();
	}

	@Override
	@Transactional
	public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
		Compilation oldCompilation = compilationRepository.findById(compId)
				.orElseThrow(() -> new CompilationNotExistException("Can't update compilation - the compilation doesn't exist"));

		if (updateCompilationRequest.getEvents() != null) {
			oldCompilation.setEventIds(new ArrayList<>(updateCompilationRequest.getEvents()));
		}

		if (updateCompilationRequest.getPinned() != null) {
			oldCompilation.setPinned(updateCompilationRequest.getPinned());
		}

		if (updateCompilationRequest.getTitle() != null) {
			oldCompilation.setTitle(updateCompilationRequest.getTitle());
		}

		Compilation updatedCompilation = compilationRepository.save(oldCompilation);
		log.debug("Compilation with ID = {} is updated", compId);
		return toDto(updatedCompilation);
	}

	@Override
	@Transactional
	public void deleteCompilation(Long compId) {
		compilationRepository.deleteById(compId);
		log.debug("Compilation with ID = {} is deleted", compId);
	}

	private CompilationDto toDto(Compilation compilation) {
		List<EventShortDto> events = compilation.getEventIds() == null || compilation.getEventIds().isEmpty()
				? List.of()
				: eventClient.getEventsByIds(compilation.getEventIds());
		return toDto(compilation, events);
	}

	private CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
		return mapper.toCompilationDto(compilation, events);
	}

	private List<EventShortDto> eventsFor(Compilation compilation, List<EventShortDto> allEvents) {
		if (compilation.getEventIds() == null || compilation.getEventIds().isEmpty() || allEvents.isEmpty()) {
			return List.of();
		}
		return allEvents.stream()
				.filter(e -> compilation.getEventIds().contains(e.getId()))
				.toList();
	}
}

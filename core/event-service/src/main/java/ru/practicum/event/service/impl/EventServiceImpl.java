package ru.practicum.event.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.Constants;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.EventFullDto;
import ru.practicum.common.dto.EventShortDto;
import ru.practicum.common.dto.EventShortInfoDto;
import ru.practicum.common.dto.UserShortDto;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.client.CategoryClient;
import ru.practicum.event.client.RequestStatsClient;
import ru.practicum.event.client.UserClient;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminDto;
import ru.practicum.event.dto.UpdateEventUserDto;
import ru.practicum.event.enumeration.SortValue;
import ru.practicum.event.enumeration.StateActionForAdmin;
import ru.practicum.event.enumeration.StateActionForUser;
import ru.practicum.event.exception.EventNotExistException;
import ru.practicum.event.exception.WrongTimeException;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.event.utils.EventPredicateUtil;
import ru.practicum.event.utils.EventValidator;
import ru.practicum.ewm.stats.action.v1.ActionTypeProto;
import ru.practicum.ewm.stats.recommendations.v1.RecommendedEventProto;
import ru.practicum.stats.client.AnalyzerClient;
import ru.practicum.stats.client.CollectorClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
	private final EventRepository eventRepository;
	private final EventValidator eventValidator;
	private final EventMapper eventMapper;
	private final ru.practicum.event.mapper.LocationMapper locationMapper;
	private final CategoryClient categoryClient;
	private final UserClient userClient;
	private final RequestStatsClient requestStatsClient;
	private final AnalyzerClient analyzerClient;
	private final CollectorClient collectorClient;
	private final EntityManager entityManager;

	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT);

	@Override
	@Transactional
	public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {

		eventValidator.validateNewEventDate(newEventDto.getEventDate());

		Event event = eventMapper.toEventModel(newEventDto);
		event.setInitiatorId(userId);

		Event savedEvent = eventRepository.save(event);

		return enrichFull(savedEvent);
	}

	@Override
	public List<EventShortDto> getEvents(Long userId, Integer from, Integer size) {
		Pageable page = PageRequest.of(from / size, size);
		List<Event> events = eventRepository.findAllByInitiatorId(userId, page).toList();
		return enrichShort(events);
	}

	@Override
	@Transactional
	public EventFullDto updateEvent(Long eventId, UpdateEventAdminDto dto) {

		Event event = getEventById(eventId);

		if (dto == null) {
			return enrichFull(event);
		}

		applyAdminUpdate(event, dto);

		if (dto.getEventDate() != null) {
			eventValidator.validateEventDateUpdate(dto.getEventDate());
			event.setEventDate(dto.getEventDate());
		}

		if (dto.getStateAction() != null) {

			eventValidator.validateAdminPublish(event);

			if (dto.getStateAction() == StateActionForAdmin.PUBLISH_EVENT) {
				event.setState(EventState.PUBLISHED);
				event.setPublishedOn(LocalDateTime.now());
			} else {
				event.setState(EventState.CANCELED);
			}
		}

		return enrichFull(eventRepository.save(event));
	}

	@Override
	@Transactional
	public EventFullDto publishEvent(Long eventId) {
		UpdateEventAdminDto dto = new UpdateEventAdminDto();
		dto.setStateAction(StateActionForAdmin.PUBLISH_EVENT);
		return updateEvent(eventId, dto);
	}

	@Override
	@Transactional
	public EventFullDto rejectEvent(Long eventId) {
		UpdateEventAdminDto dto = new UpdateEventAdminDto();
		dto.setStateAction(StateActionForAdmin.REJECT_EVENT);
		return updateEvent(eventId, dto);
	}

	@Override
	public List<EventFullDto> getPendingEvents(Integer from, Integer size) {
		return getEventsWithParamsByAdmin(null, EventState.PENDING, null, null, null, from, size);
	}

	@Override
	@Transactional
	public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserDto dto) {

		Event event = eventRepository
				.findByIdAndInitiatorId(eventId, userId)
				.orElseThrow(() -> new EventNotExistException(""));

		eventValidator.validateUserUpdate(event);

		if (dto == null) {
			return enrichFull(event);
		}

		applyUserUpdate(event, dto);

		if (dto.getEventDate() != null) {
			eventValidator.validateEventDateUpdate(dto.getEventDate());
			event.setEventDate(dto.getEventDate());
		}

		if (dto.getStateAction() != null) {

			if (dto.getStateAction() == StateActionForUser.SEND_TO_REVIEW) {
				event.setState(EventState.PENDING);
			} else {
				event.setState(EventState.CANCELED);
			}
		}

		return enrichFull(eventRepository.save(event));
	}

	@Override
	public EventFullDto getEventByUser(Long userId, Long eventId) {
		Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
				.orElseThrow(() -> new EventNotExistException(""));
		return enrichFull(event);
	}

	@Override
	public List<EventFullDto> getEventsWithParamsByAdmin(List<Long> users, EventState states, List<Long> categoriesId,
														 LocalDateTime rangeStart, LocalDateTime rangeEnd,
														 Integer from, Integer size) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Event> query = builder.createQuery(Event.class);
		Root<Event> root = query.from(Event.class);
		Predicate criteria = builder.conjunction();

		criteria = EventPredicateUtil.addCategoryFilter(criteria, builder, root, categoriesId);
		criteria = EventPredicateUtil.addUserFilter(criteria, builder, root, users);
		criteria = EventPredicateUtil.addStateFilter(criteria, builder, root, states);
		criteria = EventPredicateUtil.addDateFilter(criteria, builder, root, rangeStart, "eventDate", true);
		criteria = EventPredicateUtil.addDateFilter(criteria, builder, root, rangeEnd, "eventDate", false);

		query.select(root)
				.where(criteria)
				.orderBy(builder.desc(root.get("createdOn")));

		List<Event> events = entityManager.createQuery(query)
				.setFirstResult(from)
				.setMaxResults(size)
				.getResultList();

		if (events.isEmpty()) return List.of();

		List<EventFullDto> dtos = enrichFullList(events);
		setRating(dtos);

		return dtos;
	}

	@Override
	public List<EventFullDto> getEventsWithParamsByUser(String text, List<Long> users, List<Long> categories,
														Boolean paid, String rangeStart, String rangeEnd,
														Boolean onlyAvailable, SortValue sort, Integer from,
														Integer size, List<String> states) {

		LocalDateTime start = null;
		LocalDateTime end = null;
		try {
			if (rangeStart != null) start = LocalDateTime.parse(rangeStart, dateFormatter);
			if (rangeEnd != null) end = LocalDateTime.parse(rangeEnd, dateFormatter);
		} catch (DateTimeParseException e) {
			log.debug("Неверный формат даты: {}", e.getMessage());
		}

		checkDateTime(start, end);

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Event> cq = cb.createQuery(Event.class);
		Root<Event> root = cq.from(Event.class);

		Predicate predicate = cb.conjunction();

		predicate = EventPredicateUtil.addTextFilter(predicate, cb, root, text);
		predicate = EventPredicateUtil.addUserFilter(predicate, cb, root, users);
		predicate = EventPredicateUtil.addCategoryFilter(predicate, cb, root, categories);
		predicate = EventPredicateUtil.addPaidFilter(predicate, cb, root, paid);
		predicate = EventPredicateUtil.addDateFilter(predicate, cb, root, start, "eventDate", true);
		predicate = EventPredicateUtil.addDateFilter(predicate, cb, root, end, "eventDate", false);
		predicate = EventPredicateUtil.addStateFilter(predicate, cb, root, states);

		// Сортировку по рейтингу (RATING) выполняем в памяти после обогащения DTO —
		// rating не persist-колонка events, а вычисляется через Analyzer.
		cq.select(root).where(predicate);
		cq.orderBy(cb.asc(root.get("eventDate")));

		List<Event> events = entityManager.createQuery(cq)
				.setFirstResult(from != null ? from : 0)
				.setMaxResults(size != null ? size : 10)
				.getResultList();

		if (events.isEmpty()) return List.of();

		List<EventFullDto> dtos = enrichFullList(events);
		setRating(dtos);

		if (Boolean.TRUE.equals(onlyAvailable)) {
			dtos = dtos.stream()
					.filter(dto -> dto.getConfirmedRequests() < dto.getParticipantLimit())
					.collect(Collectors.toList());
		}

		if (sort == SortValue.RATING) {
			dtos = dtos.stream()
					.sorted(Comparator.comparingDouble(EventFullDto::getRating).reversed())
					.collect(Collectors.toList());
		}

		// По ТЗ Этапа 3-2: GET /events больше не отправляет информацию о просмотре.

		return dtos;
	}

	@Override
	public EventFullDto getEvent(Long id, Long userId) {
		Event event = eventRepository.findByIdAndPublishedOnIsNotNull(id)
				.orElseThrow(() -> new EventNotExistException(
						String.format("Can't find event with id = %s event doesn't exist", id)));

		EventFullDto eventFullDto = enrichFull(event);

		// Рейтинг — сумма максимальных весов действий (через Analyzer gRPC).
		setRating(List.of(eventFullDto));

		// По ТЗ Этапа 3-2: фиксируем просмотр пользователем мероприятия,
		// отправляя ACTION_VIEW в Collector (если передан заголовок X-EWM-USER-ID).
		if (userId != null) {
			collectorClient.collectUserActionSafe(userId, id, ActionTypeProto.ACTION_VIEW, Instant.now());
		}

		return eventFullDto;
	}

	@Override
	public boolean existsByCategoryId(Long categoryId) {
		return eventRepository.existsByCategoryId(categoryId);
	}

	@Override
	public EventShortInfoDto getEventInfo(Long eventId) {
		Event event = getEventById(eventId);
		return EventShortInfoDto.builder()
				.id(event.getId())
				.initiatorId(event.getInitiatorId())
				.state(event.getState())
				.participantLimit((long) event.getParticipantLimit())
				.requestModeration(event.getRequestModeration())
				.publishedOn(event.getPublishedOn())
				.build();
	}

	@Override
	public List<EventShortDto> getEventShortDtosByIds(List<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return List.of();
		}
		List<Event> events = eventRepository.findAllByIdIn(eventIds);
		if (events.isEmpty()) {
			return List.of();
		}
		return enrichShort(events);
	}

	@Override
	public List<EventShortDto> getRecommendations(Long userId, Integer maxResults) {
		int limit = maxResults == null || maxResults <= 0 ? 10 : maxResults;
		// 1) Запрос персональных рекомендаций у Analyzer (предсказание оценки).
		List<Long> recommendedIds = analyzerClient.getRecommendationsForUser(userId, limit)
				.map(RecommendedEventProto::getEventId)
				.toList();
		if (recommendedIds.isEmpty()) {
			return List.of();
		}
		// 2) Грузим события пачкой и обогащаем. Порядок сохраняем по recommendations.
		List<Event> events = eventRepository.findAllByIdIn(recommendedIds);
		Map<Long, Event> byId = events.stream().collect(Collectors.toMap(Event::getId, Function.identity()));
		List<Event> ordered = recommendedIds.stream()
				.map(byId::get)
				.filter(java.util.Objects::nonNull)
				.toList();
		return enrichShort(ordered);
	}

	@Override
	public void likeEvent(Long userId, Long eventId) {
		// По ТЗ: лайкать можно только посещённые мероприятия.
		boolean visited = analyzerClient.hasInteraction(userId, eventId);
		if (!visited) {
			throw new ru.practicum.event.exception.LikeNotAllowedException(
					"Пользователь может лайкать только посещённые им мероприятия");
		}
		collectorClient.collectUserActionSafe(userId, eventId, ActionTypeProto.ACTION_LIKE, Instant.now());
	}

	private Event getEventById(Long eventId) {
		return eventRepository.findById(eventId)
				.orElseThrow(() -> new EventNotExistException(
						String.format("Event with id=%s was not found", eventId)));
	}

	private void applyAdminUpdate(Event event, UpdateEventAdminDto dto) {
		if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
		if (dto.getDescription() != null) event.setDescription(dto.getDescription());
		if (dto.getLocation() != null) event.setLocation(locationMapper.toLocationModel(dto.getLocation()));
		if (dto.getPaid() != null) event.setPaid(dto.getPaid());
		if (dto.getTitle() != null) event.setTitle(dto.getTitle());
		if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
		if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit().intValue());
		if (dto.getCategory() != null) event.setCategoryId(dto.getCategory());
	}

	private void applyUserUpdate(Event event, UpdateEventUserDto dto) {
		if (dto.getAnnotation() != null) event.setAnnotation(dto.getAnnotation());
		if (dto.getDescription() != null) event.setDescription(dto.getDescription());
		if (dto.getLocation() != null) event.setLocation(locationMapper.toLocationModel(dto.getLocation()));
		if (dto.getPaid() != null) event.setPaid(dto.getPaid());
		if (dto.getTitle() != null) event.setTitle(dto.getTitle());
		if (dto.getRequestModeration() != null) event.setRequestModeration(dto.getRequestModeration());
		if (dto.getParticipantLimit() != null) event.setParticipantLimit(dto.getParticipantLimit().intValue());
		if (dto.getCategory() != null) event.setCategoryId(dto.getCategory());
	}

	// ─── Обогащение DTO данными из других сервисов (Feign) ──────────────────

	private EventFullDto enrichFull(Event event) {
		CategoryDto category = event.getCategoryId() == null ? null : categoryClient.getCategoryById(event.getCategoryId());
		UserShortDto initiator = event.getInitiatorId() == null ? null : userClient.getUserById(event.getInitiatorId());
		EventFullDto dto = eventMapper.toEventFullDto(event, category, initiator);
		dto.setConfirmedRequests(requestStatsClient.countConfirmedByEvent(event.getId(), null) == null ? 0L
				: requestStatsClient.countConfirmedByEvent(event.getId(), null));
		return dto;
	}

	private List<EventFullDto> enrichFullList(List<Event> events) {
		Map<Long, CategoryDto> categoryByEvent = fetchCategories(events);
		Map<Long, UserShortDto> initiatorByEvent = fetchInitiators(events);
		List<EventFullDto> dtos = eventMapper.toEventFullDtoList(events, categoryByEvent, initiatorByEvent);

		Map<Long, Long> confirmed = requestStatsClient.countConfirmedByEvents(
				events.stream().map(Event::getId).toList(), null);
		dtos.forEach(dto -> dto.setConfirmedRequests(confirmed.getOrDefault(dto.getId(), 0L)));

		return dtos;
	}

	private List<EventShortDto> enrichShort(List<Event> events) {
		Map<Long, CategoryDto> categoryByEvent = fetchCategories(events);
		Map<Long, UserShortDto> initiatorByEvent = fetchInitiators(events);
		return eventMapper.toEventShortDtoList(events, categoryByEvent, initiatorByEvent);
	}

	private Map<Long, CategoryDto> fetchCategories(List<Event> events) {
		List<Long> categoryIds = events.stream().map(Event::getCategoryId).filter(java.util.Objects::nonNull).distinct().toList();
		if (categoryIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return categoryClient.getCategoriesByIds(categoryIds).stream()
				.collect(Collectors.toMap(CategoryDto::getId, Function.identity()));
	}

	private Map<Long, UserShortDto> fetchInitiators(List<Event> events) {
		List<Long> userIds = events.stream().map(Event::getInitiatorId).filter(java.util.Objects::nonNull).distinct().toList();
		if (userIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return userClient.getUsersByIds(userIds).stream()
				.collect(Collectors.toMap(UserShortDto::getId, Function.identity()));
	}

	// ─── Рейтинг (через Analyzer gRPC) ────────────────────────────────────

	/**
	 * Проставляет rating в DTO событий пакетно: запрашивает у Analyzer суммы
	 * максимальных весов действий пользователей по всем событиям списка.
	 * Сбои Analyzer не должны валить показ событий — используем safe-вариант клиента.
	 */
	private void setRating(List<EventFullDto> events) {
		if (events == null || events.isEmpty()) {
			return;
		}
		List<Long> ids = events.stream().map(EventFullDto::getId).toList();
		List<RecommendedEventProto> ratings = analyzerClient.getInteractionsCountSafe(ids);
		Map<Long, Double> ratingById = ratings.stream()
				.collect(Collectors.toMap(RecommendedEventProto::getEventId,
						RecommendedEventProto::getScore, (a, b) -> a));
		events.forEach(dto -> dto.setRating(ratingById.getOrDefault(dto.getId(), 0.0)));
	}

	private void checkDateTime(LocalDateTime start, LocalDateTime end) {
		if (start == null) {
			start = LocalDateTime.now().minusYears(100);
		}
		if (end == null) {
			end = LocalDateTime.now();
		}
		if (start.isAfter(end)) {
			throw new WrongTimeException("Некорректный запрос. Дата окончания события задана позже даты старта");
		}
	}
}

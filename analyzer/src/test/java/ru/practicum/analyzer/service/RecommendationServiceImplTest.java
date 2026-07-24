package ru.practicum.analyzer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.analyzer.dto.RecommendedEvent;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.analyzer.repository.UserActionRepository.EventWeight;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

/**
 * Юнит-тесты трёх алгоритмов рекомендаций с моками репозиториев.
 *
 * <p>Проверяют ключевые инварианты:
 * <ul>
 *   <li>{@code getSimilarEvents}: исключение просмотренных + top-N по сходству;</li>
 *   <li>{@code getRecommendationsForUser}: пустой список при отсутствии взаимодействий;</li>
 *   <li>{@code getInteractionsCount}: сумма максимальных весов по мероприятиям.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

	@Mock
	private EventSimilarityRepository eventSimilarityRepository;
	@Mock
	private UserActionRepository userActionRepository;

	@InjectMocks
	private RecommendationServiceImpl service;

	@BeforeEach
	void setUp() {
		// K ближайших соседей — приватное поле @Value; ставим через рефлексию.
		try {
			var field = RecommendationServiceImpl.class.getDeclaredField("neighbors");
			field.setAccessible(true);
			field.set(service, 5);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	@Test
	void getSimilarEvents_excludesInteractedAndReturnsTopByScore() {
		// Event 1 похож на 2 (score=0.9), на 3 (score=0.5), на 4 (score=0.7).
		List<EventSimilarity> sims = List.of(
				new EventSimilarity(1L, 2L, 0.9),
				new EventSimilarity(1L, 3L, 0.5),
				new EventSimilarity(1L, 4L, 0.7));
		lenient().when(eventSimilarityRepository.findAllByEvent(1L)).thenReturn(sims);

		// Пользователь уже взаимодействовал с event 3 — он должен быть исключён.
		lenient().when(userActionRepository.findAllByUserId(100L))
				.thenReturn(List.of(userAction(100L, 3L, 0.4)));

		List<RecommendedEvent> result = service.getSimilarEvents(1L, 100L, 10);

		// Должны остаться 2 и 4, отсортированные по убыванию score: [2 (0.9), 4 (0.7)].
		assertEquals(2, result.size());
		assertEquals(2L, result.get(0).eventId());
		assertEquals(0.9, result.get(0).score(), 1e-9);
		assertEquals(4L, result.get(1).eventId());
		assertEquals(0.7, result.get(1).score(), 1e-9);
	}

	@Test
	void getSimilarEvents_emptyWhenNoSimilarity() {
		lenient().when(eventSimilarityRepository.findAllByEvent(99L)).thenReturn(List.of());
		List<RecommendedEvent> result = service.getSimilarEvents(99L, 100L, 10);
		assertTrue(result.isEmpty());
	}

	@Test
	void getSimilarEvents_emptyWhenMaxResultsZero() {
		List<RecommendedEvent> result = service.getSimilarEvents(1L, 100L, 0);
		assertTrue(result.isEmpty());
	}

	@Test
	void getRecommendationsForUser_emptyWhenUserHasNoInteractions() {
		lenient().when(userActionRepository.findAllByUserIdOrderByActionAtDesc(eq(100L), any(PageRequest.class)))
				.thenReturn(List.of());
		List<RecommendedEvent> result = service.getRecommendationsForUser(100L, 10);
		assertTrue(result.isEmpty());
	}

	@Test
	void getInteractionsCount_sumsMaxWeightsByEvent() {
		// Запрос по [1, 2, 3]. Из БД пришли суммы для 1 и 2; для 3 нет → 0.
		lenient().when(userActionRepository.sumWeightsByEvents(anyList()))
				.thenReturn(List.<EventWeight>of(
						weight(1L, 2.4),
						weight(2L, 0.8)));

		List<RecommendedEvent> result = service.getInteractionsCount(List.of(1L, 2L, 3L));

		assertEquals(3, result.size());
		assertEquals(1L, result.get(0).eventId());
		assertEquals(2.4, result.get(0).score(), 1e-9);
		assertEquals(2L, result.get(1).eventId());
		assertEquals(0.8, result.get(1).score(), 1e-9);
		assertEquals(3L, result.get(2).eventId());
		assertEquals(0.0, result.get(2).score(), 1e-9, "отсутствующее событие → 0");
	}

	@Test
	void getInteractionsCount_emptyForEmptyInput() {
		List<RecommendedEvent> result = service.getInteractionsCount(List.of());
		assertTrue(result.isEmpty());
	}

	private static UserAction userAction(long userId, long eventId, double weight) {
		return new UserAction(userId, eventId, weight, Instant.now());
	}

	private static EventWeight weight(Long id, Double total) {
		return new EventWeight() {
			@Override
			public Long getEventId() {
				return id;
			}

			@Override
			public Double getTotal() {
				return total;
			}
		};
	}
}

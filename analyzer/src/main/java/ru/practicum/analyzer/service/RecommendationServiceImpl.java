package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.dto.RecommendedEvent;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserActionRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация трёх алгоритмов рекомендаций (Этап 3-2).
 *
 * <p>Все алгоритмы оперируют данными, накопленными Kafka-consumer-ами:
 * <ul>
 *   <li>{@link EventSimilarityRepository} — коэффициенты сходства мероприятий;</li>
 *   <li>{@link UserActionRepository} — максимальные веса действий пользователей.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

	private final EventSimilarityRepository eventSimilarityRepository;
	private final UserActionRepository userActionRepository;

	/** K ближайших соседей для предсказания оценки. */
	@Value("${app.recommendations.neighbors:5}")
	private int neighbors;

	@Override
	public List<RecommendedEvent> getSimilarEvents(Long eventId, Long userId, int maxResults) {
		if (maxResults <= 0) {
			return List.of();
		}
		// 1) Все пары сходства с указанным мероприятием.
		List<EventSimilarity> pairs = eventSimilarityRepository.findAllByEvent(eventId);
		if (pairs.isEmpty()) {
			return List.of();
		}

		// 2) Исключаем мероприятия, с которыми пользователь уже взаимодействовал.
		Set<Long> interacted = userActionRepository.findAllByUserId(userId).stream()
				.map(UserAction::getEventId)
				.collect(Collectors.toSet());

		// 3) Топ N по коэффициенту сходства.
		return pairs.stream()
				.map(p -> toOtherEvent(p, eventId))
				.filter(r -> !interacted.contains(r.eventId()))
				.sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
				.limit(maxResults)
				.toList();
	}

	@Override
	public List<RecommendedEvent> getRecommendationsForUser(Long userId, int maxResults) {
		if (maxResults <= 0) {
			return List.of();
		}
		// Шаг 1: последние N взаимодействий пользователя (от новых к старым).
		List<UserAction> userActions = userActionRepository.findAllByUserIdOrderByActionAtDesc(
				userId, PageRequest.of(0, maxResults));
		if (userActions.isEmpty()) {
			// Не с чем строить рекомендации.
			return List.of();
		}

		Set<Long> interacted = userActions.stream()
				.map(UserAction::getEventId)
				.collect(Collectors.toSet());
		Map<Long, Double> userWeightByEvent = userActions.stream()
				.collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight, (a, b) -> a));

		// Шаг 1.2: найти похожие непросмотренные мероприятия для каждого просмотренного.
		List<Long> seedEventIds = new ArrayList<>(interacted);
		List<EventSimilarity> similarities = eventSimilarityRepository.findAllByAnyEvent(seedEventIds);

		// candidateEventId → максимальный коэффициент сходства с каким-либо просмотренным.
		Map<Long, Double> bestSimilarityByCandidate = new HashMap<>();
		for (EventSimilarity s : similarities) {
			Long seed = resolveSeed(s, seedEventIds);
			Long candidate = resolveOther(s, seed);
			if (seed == null || candidate == null || interacted.contains(candidate)) {
				continue;
			}
			bestSimilarityByCandidate.merge(candidate, s.getScore(), Math::max);
		}

		if (bestSimilarityByCandidate.isEmpty()) {
			return List.of();
		}

		// Шаг 1.3: выбираем top N кандидатов по сходству.
		List<Long> topCandidates = bestSimilarityByCandidate.entrySet().stream()
				.sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
				.limit(maxResults)
				.map(Map.Entry::getKey)
				.toList();

		// Шаг 2: предсказываем оценку для каждого кандидата.
		// ŷ = Σ(sim(candidate, j) · r_user_j) / Σ(sim(candidate, j)) по K ближайшим просмотренным соседям.
		List<EventSimilarity> candidateSims = eventSimilarityRepository.findAllByAnyEvent(topCandidates);
		Map<Long, List<EventSim>> simsByCandidate = new HashMap<>();
		for (EventSimilarity s : candidateSims) {
			Long candidate = resolveSeed(s, topCandidates);
			if (candidate == null) {
				continue;
			}
			Long other = resolveOther(s, candidate);
			if (!interacted.contains(other)) {
				continue;
			}
			simsByCandidate.computeIfAbsent(candidate, k -> new ArrayList<>())
					.add(new EventSim(other, s.getScore()));
		}

		List<RecommendedEvent> result = new ArrayList<>();
		for (Long candidate : topCandidates) {
			List<EventSim> sims = simsByCandidate.getOrDefault(candidate, List.of());
			// K ближайших соседей (по сходству).
			List<EventSim> nearest = sims.stream()
					.sorted(Comparator.comparingDouble(es -> -es.score))
					.limit(neighbors)
					.toList();

			double weightedSum = 0.0;
			double simSum = 0.0;
			for (EventSim es : nearest) {
				Double userWeight = userWeightByEvent.get(es.eventId);
				if (userWeight == null) {
					continue;
				}
				weightedSum += es.score * userWeight;
				simSum += es.score;
			}
			if (simSum <= 0.0) {
				continue;
			}
			double predicted = weightedSum / simSum;
			result.add(new RecommendedEvent(candidate, predicted));
		}

		// Сортируем по убыванию предсказанной оценки и ограничиваем maxResults.
		return result.stream()
				.sorted(Comparator.comparingDouble(RecommendedEvent::score).reversed())
				.limit(maxResults)
				.toList();
	}

	@Override
	public List<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return List.of();
		}
		List<UserActionRepository.EventWeight> rows = userActionRepository.sumWeightsByEvents(eventIds);
		Map<Long, Double> map = new HashMap<>();
		for (UserActionRepository.EventWeight w : rows) {
			map.put(w.getEventId(), w.getTotal());
		}
		// Возвращаем в порядке запроса; для отсутствующих — 0.0.
		return eventIds.stream()
				.map(id -> new RecommendedEvent(id, map.getOrDefault(id, 0.0)))
				.toList();
	}

	@Override
	public boolean hasInteraction(Long userId, Long eventId) {
		return userActionRepository.existsByUserIdAndEventId(userId, eventId);
	}

	// ─── Утилиты ─────────────────────────────────────────────────────────

	/** Возвращает «другое» мероприятие из пары сходства, кроме {@code eventId}. */
	private static RecommendedEvent toOtherEvent(EventSimilarity s, Long eventId) {
		Long other = s.getEventA().equals(eventId) ? s.getEventB() : s.getEventA();
		return new RecommendedEvent(other, s.getScore());
	}

	/** Возвращает «посевное» мероприятие пары, если оно есть в {@code seeds}. */
	private static Long resolveSeed(EventSimilarity s, List<Long> seeds) {
		Set<Long> seedSet = new HashSet<>(seeds);
		if (seedSet.contains(s.getEventA())) {
			return s.getEventA();
		}
		if (seedSet.contains(s.getEventB())) {
			return s.getEventB();
		}
		return null;
	}

	/** Возвращает «другое» мероприятие пары относительно {@code seed}. */
	private static Long resolveOther(EventSimilarity s, Long seed) {
		if (seed == null) {
			return null;
		}
		return s.getEventA().equals(seed) ? s.getEventB() : s.getEventA();
	}

	/** Внутренная пара (событие, сходство) для K ближайших соседей. */
	private record EventSim(Long eventId, double score) {
	}
}

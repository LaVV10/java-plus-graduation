package ru.practicum.analyzer.service;

import ru.practicum.analyzer.dto.RecommendedEvent;

import java.util.List;

/**
 * Сервис рекомендаций мероприятий. Реализует три алгоритма gRPC-сервиса
 * {@code RecommendationsController}.
 */
public interface RecommendationService {

	/**
	 * Мероприятия, похожие на указанное: выгружает все пары сходства с данным событием,
	 * исключает те, что пользователь уже просмотрел, и возвращает top {@code maxResults}
	 * по коэффициенту сходства.
	 */
	List<RecommendedEvent> getSimilarEvents(Long eventId, Long userId, int maxResults);

	/**
	 * Персональные рекомендации на основе предсказания оценки:
	 * последние N взаимодействий пользователя → найти похожие непросмотренные →
	 * для каждого вычислить предсказанную оценку через K ближайших просмотренных соседей.
	 */
	List<RecommendedEvent> getRecommendationsForUser(Long userId, int maxResults);

	/**
	 * Сумма максимальных весов взаимодействий по набору мероприятий
	 * (для поля {@code rating} у события в event-service).
	 */
	List<RecommendedEvent> getInteractionsCount(List<Long> eventIds);

	/**
	 * Проверяет, взаимодействовал ли пользователь с мероприятием ранее
	 * (для валидации лайка в event-service).
	 */
	boolean hasInteraction(Long userId, Long eventId);
}

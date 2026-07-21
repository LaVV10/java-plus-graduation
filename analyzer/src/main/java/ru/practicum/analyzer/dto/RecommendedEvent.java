package ru.practicum.analyzer.dto;

/**
 * DTO рекомендации мероприятия (внутренний формат Analyzer).
 *
 * <p>{@code score} имеет разный смысл в зависимости от алгоритма:
 * <ul>
 *   <li>{@code GetRecommendationsForUser} — предсказанная оценка;</li>
 *   <li>{@code GetSimilarEvents} — коэффициент сходства;</li>
 *   <li>{@code GetInteractionsCount} — сумма максимальных весов действий.</li>
 * </ul>
 *
 * @param eventId идентификатор рекомендуемого мероприятия
 * @param score   оценка/сходство/сумма весов
 */
public record RecommendedEvent(Long eventId, double score) {
}

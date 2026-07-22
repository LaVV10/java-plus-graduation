package ru.practicum.aggregator.similarity;

import org.junit.jupiter.api.Test;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Юнит-тесты математики косинусного сходства (инкрементальная форма по ТЗ Этапа 3-1).
 *
 * <p>Проверяет: веса действий, формулу {@code similarity = S_min / (sqrt(S_a) * sqrt(S_b))},
 * граничные случаи (нулевые суммы), а также сквозной ручной сценарий пересчёта
 * частных сумм при приходе новых действий — именно то, что делает SimilarityProcessor.
 */
class SimilarityCalculatorTest {

	@Test
	void weightOf_returnsExpectedWeightsByAction() {
		assertEquals(0.4, SimilarityCalculator.weightOf(ActionTypeAvro.VIEW));
		assertEquals(0.8, SimilarityCalculator.weightOf(ActionTypeAvro.REGISTER));
		assertEquals(1.0, SimilarityCalculator.weightOf(ActionTypeAvro.LIKE));
	}

	@Test
	void similarity_zero_whenAnySumIsZero() {
		// Если хотя бы одно мероприятие не имеет взаимодействий — сходство 0.
		assertEquals(0.0, SimilarityCalculator.similarity(0.5, 0.0, 1.0), 1e-9);
		assertEquals(0.0, SimilarityCalculator.similarity(0.5, 1.0, 0.0), 1e-9);
		assertEquals(0.0, SimilarityCalculator.similarity(0.5, 0.0, 0.0), 1e-9);
	}

	@Test
	void similarity_matchesManualComputation() {
		// Сценарий: 2 пользователя, оба взаимодействовали с обоими мероприятиями A и B.
		// w_u1_A = 0.8 (REGISTER), w_u1_B = 0.4 (VIEW)
		// w_u2_A = 1.0 (LIKE),     w_u2_B = 0.8 (REGISTER)
		// S_min(A,B) = min(0.8,0.4) + min(1.0,0.8) = 0.4 + 0.8 = 1.2
		// S_A = 0.8 + 1.0 = 1.8 ; S_B = 0.4 + 0.8 = 1.2
		// similarity = 1.2 / (sqrt(1.8) * sqrt(1.2)) = 1.2 / (1.342 * 1.095) ≈ 0.816
		double sMin = 1.2;
		double sA = 1.8;
		double sB = 1.2;
		double expected = sMin / (Math.sqrt(sA) * Math.sqrt(sB));
		assertEquals(expected, SimilarityCalculator.similarity(sMin, sA, sB), 1e-9);
		assertTrue(expected > 0.8 && expected < 0.85, "сходство должно попасть в (0.8, 0.85)");
	}

	@Test
	void similarity_isInUnitRange_forTypicalWeights() {
		// Косинусное сходство по построению ∈ [0,1]. Проверяем для типичных значений.
		double score = SimilarityCalculator.similarity(0.3, 0.8, 0.6);
		assertTrue(score >= 0.0 && score <= 1.0, "сходство вне [0,1]: " + score);
	}

	/**
	 * Сквозной сценарий инкрементального обновления «вручную», повторяющий логику
	 * SimilarityProcessor, но без Kafka. Гарантирует, что формула сходится к
	 * ожидаемому значению после серии обновлений.
	 */
	@Test
	void incrementalUpdate_convergesToFullRecalculation() {
		// 2 мероприятия (A=1, B=2), 2 пользователя.
		// Подадим действия по очереди и просуммируем частные суммы так же, как processor.
		java.util.Map<String, Double> userAction = new java.util.HashMap<>();
		java.util.Map<Integer, Double> eventWeights = new java.util.HashMap<>();
		java.util.Map<String, Double> similarity = new java.util.HashMap<>();

		// u1: VIEW на A (0.4)
		apply(userAction, eventWeights, similarity, 1, 1, ActionTypeAvro.VIEW);
		// u1: VIEW на B (0.4)
		apply(userAction, eventWeights, similarity, 1, 2, ActionTypeAvro.VIEW);
		// u1: REGISTER на A (0.8) — повышает вес до 0.8
		apply(userAction, eventWeights, similarity, 1, 1, ActionTypeAvro.REGISTER);
		// u2: LIKE на A (1.0)
		apply(userAction, eventWeights, similarity, 2, 1, ActionTypeAvro.LIKE);
		// u2: REGISTER на B (0.8)
		apply(userAction, eventWeights, similarity, 2, 2, ActionTypeAvro.REGISTER);

		// Финальные веса: u1:A=0.8, u1:B=0.4, u2:A=1.0, u2:B=0.8
		double sA = 0.8 + 1.0; // = 1.8
		double sB = 0.4 + 0.8; // = 1.2
		double sMinExpected = Math.min(0.8, 0.4) + Math.min(1.0, 0.8); // = 1.2
		double scoreExpected = sMinExpected / (Math.sqrt(sA) * Math.sqrt(sB));

		double sMinActual = similarity.getOrDefault("1:2", 0.0);
		double sAactual = eventWeights.getOrDefault(1, 0.0);
		double sBactual = eventWeights.getOrDefault(2, 0.0);

		assertEquals(sA, sAactual, 1e-9, "S_A должен совпасть");
		assertEquals(sB, sBactual, 1e-9, "S_B должен совпасть");
		assertEquals(sMinExpected, sMinActual, 1e-9, "S_min(A,B) должен совпасть");

		double scoreActual = SimilarityCalculator.similarity(sMinActual, sAactual, sBactual);
		assertEquals(scoreExpected, scoreActual, 1e-9);
	}

	/**
	 * Применяет одно действие пользователя, инкрементально обновляя частные суммы,
	 * в точности повторяя логику SimilarityProcessor (только для пары событий A,B).
	 * Считаем, что каждый пользователь взаимодействует ровно с двумя событиями {1,2}.
	 */
	@SuppressWarnings("checkstyle:ParameterNumber")
	private static void apply(java.util.Map<String, Double> userAction,
							  java.util.Map<Integer, Double> eventWeights,
							  java.util.Map<String, Double> similarity,
							  int userId, int eventId, ActionTypeAvro action) {
		double newWeight = SimilarityCalculator.weightOf(action);
		String uaKey = userId + ":" + eventId;
		double oldWeight = userAction.getOrDefault(uaKey, 0.0);
		if (newWeight <= oldWeight) {
			return;
		}
		double delta = newWeight - oldWeight;
		userAction.put(uaKey, newWeight);
		eventWeights.merge(eventId, delta, Double::sum);

		int other = (eventId == 1) ? 2 : 1;
		Double wOtherBoxed = userAction.get(userId + ":" + other);
		double wOther = wOtherBoxed == null ? 0.0 : wOtherBoxed;
		double dSMin = Math.min(newWeight, wOther) - Math.min(oldWeight, wOther);
		similarity.merge("1:2", dSMin, Double::sum);
	}
}

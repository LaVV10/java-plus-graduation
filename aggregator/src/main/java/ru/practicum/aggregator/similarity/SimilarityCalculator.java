package ru.practicum.aggregator.similarity;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

/**
 * Веса действий пользователей и косинусное сходство мероприятий.
 */
public final class SimilarityCalculator {

	public static final double WEIGHT_VIEW = 0.4;
	public static final double WEIGHT_REGISTER = 0.8;
	public static final double WEIGHT_LIKE = 1.0;

	private SimilarityCalculator() {
	}

	public static double weightOf(ActionTypeAvro action) {
		return switch (action) {
			case VIEW -> WEIGHT_VIEW;
			case REGISTER -> WEIGHT_REGISTER;
			case LIKE -> WEIGHT_LIKE;
		};
	}

	/**
	 * Косинусное сходство: {@code S_min(A,B) / (sqrt(S_A) * sqrt(S_B))}.
	 */
	public static double similarity(double sMinA, double sA, double sB) {
		if (sA <= 0.0 || sB <= 0.0) {
			return 0.0;
		}
		return sMinA / (Math.sqrt(sA) * Math.sqrt(sB));
	}
}

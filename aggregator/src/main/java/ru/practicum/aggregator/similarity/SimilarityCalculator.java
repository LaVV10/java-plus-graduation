package ru.practicum.aggregator.similarity;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public final class SimilarityCalculator {

	/** Вес просмотра (неявный интерес, минимальный). */
	public static final double WEIGHT_VIEW = 0.4;
	/** Вес регистрации (неявный интерес, средний). */
	public static final double WEIGHT_REGISTER = 0.8;
	/** Вес лайка (явный интерес, максимальный). */
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

	public static double similarity(double sMinA, double sA, double sB) {
		if (sA <= 0.0 || sB <= 0.0) {
			return 0.0;
		}
		return sMinA / (Math.sqrt(sA) * Math.sqrt(sB));
	}
}

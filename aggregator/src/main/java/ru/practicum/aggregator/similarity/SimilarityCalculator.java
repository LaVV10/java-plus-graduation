package ru.practicum.aggregator.similarity;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

/**
 * Веса действий пользователей с мероприятием (по ТЗ Этапа 3-1).
 *
 * Просмотр карточки мероприятия — наименьший уровень заинтересованности (0.4).
 * Регистрация на мероприятие — средний уровень (0.8).
 * Лайк после посещения — максимальный уровень (1.0).
 *
 * Для одного пользователя и мероприятия берётся МАКСИМАЛЬНЫЙ вес из всех выполненных
 * действий (не сумма): так суммарный «вес» пользователя не зависит от количества действий,
 * а отражает истинный интерес.
 */
public final class SimilarityCalculator {

	/** Вес просмотра (неявный интерес, минимальный). */
	public static final double WEIGHT_VIEW = 0.4;
	/** Вес регистрации (неявный интерес, средний). */
	public static final double WEIGHT_REGISTER = 0.8;
	/** Вес лайка (явный интерес, максимальный). */
	public static final double WEIGHT_LIKE = 1.0;

	private SimilarityCalculator() {
	}

	/**
	 * Возвращает вес действия по его типу.
	 */
	public static double weightOf(ActionTypeAvro action) {
		return switch (action) {
			case VIEW -> WEIGHT_VIEW;
			case REGISTER -> WEIGHT_REGISTER;
			case LIKE -> WEIGHT_LIKE;
		};
	}

	/**
	 * Косинусное сходство мероприятий A и B через инкрементальные частные суммы.
	 *
	 * <pre>
	 * similarity(A,B) = S_min(A,B) / (S_A * S_B)
	 * </pre>
	 * где S_min — сумма минимальных весов пользователей, взаимодействовавших с обоими,
	 * S_A / S_B — суммы весов всех пользователей по A и B соответственно.
	 *
	 * @param sMinA текущая сумма минимальных весов S_min(A,B)
	 * @param sA    текущая сумма весов по мероприятию A (S_A)
	 * @param sB    текущая сумма весов по мероприятию B (S_B)
	 * @return значение сходства в диапазоне [0, 1]; 0 при нулевых суммах
	 */
	public static double similarity(double sMinA, double sA, double sB) {
		if (sA <= 0.0 || sB <= 0.0) {
			return 0.0;
		}
		return sMinA / (sA * sB);
	}
}

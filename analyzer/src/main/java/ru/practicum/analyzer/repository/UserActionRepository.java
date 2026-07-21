package ru.practicum.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.model.UserActionId;

import java.util.List;

/**
 * Репозиторий максимальных весов действий пользователей.
 */
@Repository
public interface UserActionRepository extends JpaRepository<UserAction, UserActionId> {

	/**
	 * Мероприятия, с которыми пользователь уже взаимодействовал, отсортированные
	 * от новых к старым. {@code Pageable} ограничивает N последними.
	 */
	List<UserAction> findAllByUserIdOrderByActionAtDesc(Long userId, Pageable pageable);

	/** Все действия пользователя (для исключения просмотренного). */
	List<UserAction> findAllByUserId(Long userId);

	/** Проверка, что пользователь взаимодействовал с мероприятием (для валидации лайка). */
	boolean existsByUserIdAndEventId(Long userId, Long eventId);

	/**
	 * Сумма максимальных весов всех пользователей по мероприятию
	 * (для {@code GetInteractionsCount}).
	 */
	@Query("select coalesce(sum(u.weight), 0.0) from UserAction u where u.eventId = :eventId")
	Double sumWeightsByEvent(@Param("eventId") Long eventId);

	/**
	 * Сумма весов по списку мероприятий. Возвращает {@code [eventId, sumWeight]} строки.
	 */
	@Query("select u.eventId as eventId, coalesce(sum(u.weight), 0.0) as total "
			+ "from UserAction u where u.eventId in :eventIds group by u.eventId")
	List<EventWeight> sumWeightsByEvents(@Param("eventIds") List<Long> eventIds);

	/** Проекция суммы весов по мероприятию. */
	interface EventWeight {
		Long getEventId();

		Double getTotal();
	}
}

package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.EventSimilarityId;

import java.util.List;

/**
 * Репозиторий сходства мероприятий.
 *
 * Используется алгоритмами {@code GetSimilarEvents} и {@code GetRecommendationsForUser}.
 */
@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarityId> {

	/**
	 * Все пары сходства, в которых участвует мероприятие {@code eventId} (в любой роли A или B).
	 * Возвращает сущность, приведённую к виду (eventId, otherEvent, score).
	 */
	@Query("select e from EventSimilarity e where e.eventA = :eventId or e.eventB = :eventId")
	List<EventSimilarity> findAllByEvent(@Param("eventId") Long eventId);

	/**
	 * Все записи сходства, где один из пары — любое из {@code eventIds}.
	 */
	@Query("select e from EventSimilarity e where e.eventA in :eventIds or e.eventB in :eventIds")
	List<EventSimilarity> findAllByAnyEvent(@Param("eventIds") List<Long> eventIds);
}

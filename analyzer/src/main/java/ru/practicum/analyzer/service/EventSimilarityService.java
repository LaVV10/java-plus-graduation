package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.EventSimilarityId;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Сохранение сходства мероприятий в БД.
 *
 * <p>По приходу {@link EventSimilarityAvro} из Kafka upsert-ит запись (eventA, eventB):
 * обновляет score, либо создаёт новую.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventSimilarityService {

	private final EventSimilarityRepository eventSimilarityRepository;

	@Transactional
	public void save(EventSimilarityAvro avro) {
		if (avro == null) {
			return;
		}
		Long eventA = avro.getEventA();
		Long eventB = avro.getEventB();
		EventSimilarityId id = new EventSimilarityId(eventA, eventB);
		EventSimilarity entity = eventSimilarityRepository.findById(id).orElseGet(EventSimilarity::new);
		entity.setEventA(eventA);
		entity.setEventB(eventB);
		entity.setScore(avro.getScore());
		eventSimilarityRepository.save(entity);
	}
}

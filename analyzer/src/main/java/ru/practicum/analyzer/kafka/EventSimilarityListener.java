package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.EventSimilarityService;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

/**
 * Слушатель Kafka-топика {@code stats.events-similarity.v1}.
 *
 * Каждое сообщение {@link EventSimilarityAvro} upsert-ит коэффициент сходства пары
 * мероприятий (см. {@link EventSimilarityService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityListener {

	private final EventSimilarityService eventSimilarityService;

	@KafkaListener(
			topics = "${app.kafka.topics.events-similarity:stats.events-similarity.v1}",
			containerFactory = "eventSimilarityContainerFactory")
	public void onEventSimilarity(EventSimilarityAvro avro) {
		log.debug("Получено EventSimilarityAvro: {}", avro);
		eventSimilarityService.save(avro);
	}
}

package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.UserActionService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Слушатель Kafka-топика {@code stats.user-actions.v1}.
 *
 * Каждое сообщение {@link UserActionAvro} аккумулируется как максимальный вес
 * действия пользователя с мероприятием (см. {@link UserActionService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionListener {

	private final UserActionService userActionService;

	@KafkaListener(
			topics = "${app.kafka.topics.user-actions:stats.user-actions.v1}",
			containerFactory = "userActionContainerFactory")
	public void onUserAction(UserActionAvro avro) {
		log.debug("Получено UserActionAvro: {}", avro);
		userActionService.save(avro);
	}
}

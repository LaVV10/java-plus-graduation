package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.model.UserActionId;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

/**
 * Сохранение максимальных весов действий пользователей в БД.
 *
 * <p>По приходу {@link UserActionAvro} из Kafka upsert-ит запись (userId, eventId):
 * если действие весомее уже сохранённого — обновляет вес и timestamp, иначе игнорирует.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionService {

	private final UserActionRepository userActionRepository;

	@Transactional
	public void save(UserActionAvro avro) {
		if (avro == null) {
			return;
		}
		Long userId = avro.getUserId();
		Long eventId = avro.getEventId();
		double weight = weightOf(avro.getActionType());

		UserActionId id = new UserActionId(userId, eventId);
		UserAction existing = userActionRepository.findById(id).orElse(null);

		if (existing != null && existing.getWeight() >= weight) {
			// Старый вес не меньше нового — не обновляем (по ТЗ).
			return;
		}

		UserAction entity = existing != null ? existing : new UserAction();
		entity.setUserId(userId);
		entity.setEventId(eventId);
		entity.setWeight(weight);
		entity.setActionAt(avro.getTimestamp() == null ? java.time.Instant.now() : avro.getTimestamp());
		userActionRepository.save(entity);
	}

	private static double weightOf(ru.practicum.ewm.stats.avro.ActionTypeAvro type) {
		return switch (type) {
			case VIEW -> 0.4;
			case REGISTER -> 0.8;
			case LIKE -> 1.0;
		};
	}
}

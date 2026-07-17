package ru.practicum.event.utils;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Утилита построения предикатов JPA Criteria для событий.
 *
 * В отличие от монолита, фильтры по категории и инициатору работают по скалярным полям
 * categoryId/initiatorId (in-предикат по id) — JOIN к таблицам categories/users больше не нужен,
 * т.к. эти сущности живут в других сервисах.
 */
public class EventPredicateUtil {

	public static Predicate addCategoryFilter(Predicate predicate, CriteriaBuilder cb,
											  Root<Event> root, List<Long> categoryIds) {
		if (categoryIds != null && !categoryIds.isEmpty()) {
			return cb.and(predicate, root.get("categoryId").in(categoryIds));
		}
		return predicate;
	}

	public static Predicate addUserFilter(Predicate predicate, CriteriaBuilder cb,
										  Root<Event> root, List<Long> userIds) {
		if (userIds != null && !userIds.isEmpty()) {
			return cb.and(predicate, root.get("initiatorId").in(userIds));
		}
		return predicate;
	}

	public static Predicate addStateFilter(Predicate predicate, CriteriaBuilder cb,
										   Root<Event> root, Object states) {
		if (states != null) {
			if (states instanceof List && !((List<?>) states).isEmpty()) {
				CriteriaBuilder.In<String> inStates = cb.in(root.get("state"));
				((List<String>) states).forEach(inStates::value);
				return cb.and(predicate, inStates);
			} else {
				return cb.and(predicate, root.get("state").in(states));
			}
		}
		return predicate;
	}

	public static Predicate addDateFilter(Predicate predicate, CriteriaBuilder cb,
										  Root<Event> root, LocalDateTime date,
										  String field, boolean isStart) {
		if (date != null) {
			return isStart
					? cb.and(predicate, cb.greaterThanOrEqualTo(root.get(field), date))
					: cb.and(predicate, cb.lessThanOrEqualTo(root.get(field), date));
		}
		return predicate;
	}

	public static Predicate addTextFilter(Predicate predicate, CriteriaBuilder cb,
										  Root<Event> root, String text) {
		if (text != null && !text.isBlank()) {
			Predicate annotation = cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%");
			Predicate description = cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%");
			return cb.and(predicate, cb.or(annotation, description));
		}
		return predicate;
	}

	public static Predicate addPaidFilter(Predicate predicate, CriteriaBuilder cb,
										  Root<Event> root, Boolean paid) {
		if (paid != null) {
			return cb.and(predicate, paid ? cb.isTrue(root.get("paid")) : cb.isFalse(root.get("paid")));
		}
		return predicate;
	}
}

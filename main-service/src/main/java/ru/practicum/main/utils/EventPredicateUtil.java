package ru.practicum.main.utils;

import jakarta.persistence.criteria.*;
import ru.practicum.main.model.Category;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class EventPredicateUtil {

    public static Predicate addCategoryFilter(Predicate predicate, CriteriaBuilder cb,
                                              Root<Event> root, List<Long> categoryIds,
                                              List<Category> categories) {
        if (categoryIds != null && !categoryIds.isEmpty() && categories != null) {
            return cb.and(predicate, root.get("category").in(categories));
        }
        return predicate;
    }

    public static Predicate addUserFilter(Predicate predicate, CriteriaBuilder cb,
                                          Root<Event> root, List<Long> userIds,
                                          List<User> users) {
        if (userIds != null && !userIds.isEmpty()) {
            if (users != null) {
                return cb.and(predicate, root.get("initiator").in(users));
            } else {
                return cb.and(predicate, root.get("initiator").get("id").in(userIds));
            }
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
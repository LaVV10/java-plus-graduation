package ru.practicum.event.utils;

import org.springframework.stereotype.Component;
import ru.practicum.common.enums.EventState;
import ru.practicum.event.exception.AlreadyPublishedException;
import ru.practicum.event.exception.EventAlreadyCanceledException;
import ru.practicum.event.exception.WrongTimeException;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;

@Component
public class EventValidator {

	public void validateNewEventDate(LocalDateTime eventDate) {
		if (eventDate == null ||
				eventDate.isBefore(LocalDateTime.now().plusHours(2))) {

			throw new WrongTimeException(
					"Field: eventDate. Error: должно содержать дату, которая еще не наступила."
			);
		}
	}

	public void validateAdminPublish(Event event) {

		if (event.getPublishedOn() != null) {
			throw new AlreadyPublishedException("Event already published");
		}

		if (event.getState() == null || event.getState() == EventState.CANCELED) {
			throw new EventAlreadyCanceledException("Event already canceled");
		}
	}

	public void validateUserUpdate(Event event) {
		if (event.getPublishedOn() != null) {
			throw new AlreadyPublishedException("Event already published");
		}
	}

	public void validateEventDateUpdate(LocalDateTime eventDate) {

		if (eventDate == null) return;

		if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
			throw new WrongTimeException(
					"The start date of the event to be modified is less than two hours from now."
			);
		}
	}
}

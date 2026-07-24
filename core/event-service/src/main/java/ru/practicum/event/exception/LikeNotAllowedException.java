package ru.practicum.event.exception;

/**
 * Лайк недопустим: пользователь пытается лайкнуть мероприятие, которое не посещал.
 * По ТЗ Этапа 3-2 возвращается как {@code 400 BAD_REQUEST}.
 */
public class LikeNotAllowedException extends RuntimeException {
	public LikeNotAllowedException(String message) {
		super(message);
	}
}

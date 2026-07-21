package ru.practicum.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.Constants;

import java.time.LocalDateTime;

/**
 * Краткий DTO события. Кросс-сервисный контракт: event-service (владелец) ← compilation-service
 * (для подборок compilations). Поля category/initiator — вложенные общие DTO.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventShortDto {
	private Long id;
	private String annotation;
	private CategoryDto category;
	private Long confirmedRequests;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private LocalDateTime eventDate;

	private UserShortDto initiator;
	private Boolean paid;
	private String title;

	/**
	 * Рейтинг мероприятия — сумма максимальных весов действий пользователей,
	 * запрашивается у Analyzer через gRPC. Заменил поле views на Этапе 3-2.
	 */
	private Double rating;

	/** По умолчанию счётчики неизвестны — null не должен торчать наружу. */
	@JsonSetter(nulls = Nulls.SKIP)
	public void setConfirmedRequests(Long confirmedRequests) {
		this.confirmedRequests = confirmedRequests == null ? 0L : confirmedRequests;
	}

	@JsonSetter(nulls = Nulls.SKIP)
	public void setRating(Double rating) {
		this.rating = rating == null ? 0.0 : rating;
	}
}

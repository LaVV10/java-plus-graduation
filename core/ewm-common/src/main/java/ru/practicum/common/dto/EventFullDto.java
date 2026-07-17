package ru.practicum.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.Constants;
import ru.practicum.common.enums.EventState;

import java.time.LocalDateTime;

/**
 * Полный DTO события. Возвращается наружу через gateway, а также является внутренним
 * контрактом event-service ← compilation-service (для подборок берётся EventShortDto,
 * но DTO события принадлежат event-service и живут в общем модуле).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventFullDto {
	private String annotation;
	private CategoryDto category;
	private Long confirmedRequests;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private String createdOn;
	private String description;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private LocalDateTime eventDate;
	private Long id;
	private UserShortDto initiator;
	private LocationDto location;
	private Boolean paid;
	private Long participantLimit;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private LocalDateTime publishedOn;
	private Boolean requestModeration;
	private EventState state;
	private String title;
	private Long views;

	/** null-сейф: счётчики не должны торчать как null наружу. */
	@JsonSetter(nulls = Nulls.SKIP)
	public void setConfirmedRequests(Long confirmedRequests) {
		this.confirmedRequests = confirmedRequests == null ? 0L : confirmedRequests;
	}

	@JsonSetter(nulls = Nulls.SKIP)
	public void setViews(Long views) {
		this.views = views == null ? 0L : views;
	}
}

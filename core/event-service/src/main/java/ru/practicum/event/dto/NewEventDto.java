package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.Constants;
import ru.practicum.common.dto.LocationDto;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewEventDto {
	@NotBlank
	@Size(min = 20, max = 2000)
	private String annotation;

	@NotNull
	private Long category;

	@NotBlank
	@Size(min = 20, max = 7000)
	private String description;

	@NotNull
	@Future
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private LocalDateTime eventDate;

	@NotNull
	private LocationDto location;
	private boolean paid;

	@PositiveOrZero
	private int participantLimit = 0;
	private Boolean requestModeration;

	@NotBlank
	@Size(min = 3, max = 120)
	private String title;
}

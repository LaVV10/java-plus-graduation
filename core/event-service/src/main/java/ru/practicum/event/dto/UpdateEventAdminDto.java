package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.Constants;
import ru.practicum.common.dto.LocationDto;
import ru.practicum.event.enumeration.StateActionForAdmin;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateEventAdminDto {
	@Size(min = 20, max = 2000)
	private String annotation;
	private Long category;

	@Size(min = 20, max = 7000)
	private String description;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_FORMAT)
	private LocalDateTime eventDate;
	private LocationDto location;
	private Boolean paid;
	private Long participantLimit;
	private Boolean requestModeration;
	private StateActionForAdmin stateAction;

	@Size(min = 3, max = 120)
	private String title;
}

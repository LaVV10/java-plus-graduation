package ru.practicum.main.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.enumeration.StateActionForUser;
import ru.practicum.main.model.Location;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateEventUserDto {
    @Size(min = 20, max = 2000)
    private String annotation;

    @Positive
    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ConfigUtil.DATE)
    private LocalDateTime eventDate;
    private Location location;
    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;
    private Boolean requestModeration;
    private StateActionForUser stateAction;

    @Size(min = 3, max = 120)
    private String title;
}
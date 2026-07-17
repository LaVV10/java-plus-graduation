package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Геолокация события. Принадлежит event-service, но DTO общий (используется в EventFullDto).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LocationDto {
	private Float lat;
	private Float lon;
}

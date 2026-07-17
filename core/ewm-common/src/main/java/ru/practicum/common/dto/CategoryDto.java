package ru.practicum.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Категория события. Кросс-сервисный контракт: feature-service (владелец) ← event-service.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
	private Long id;

	@NotBlank
	@Size(max = 50)
	private String name;
}

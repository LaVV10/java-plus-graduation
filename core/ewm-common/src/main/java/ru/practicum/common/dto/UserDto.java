package ru.practicum.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Полный DTO пользователя (admin). Кросс-сервисный контракт: user-service (владелец).
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
	private Long id;

	@NotBlank
	@Size(min = 2, max = 250)
	private String name;

	@Email
	@NotNull
	@Size(min = 6, max = 254)
	private String email;
}

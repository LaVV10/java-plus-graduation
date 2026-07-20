package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Краткий DTO пользователя (инициатор события). Кросс-сервисный контракт:
 * user-service (владелец) ← event-service, request-service.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserShortDto {
	private Long id;
	private String name;
}

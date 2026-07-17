package ru.practicum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.enums.EventState;

import java.time.LocalDateTime;

/**
 * Снимок события для межсервисного взаимодействия event-service → request-service.
 * Содержит только поля, необходимые request-service для создания/модерации заявок:
 * проверка состояния, лимита участников, необходимости премодерации, автора.
 *
 * Используется чтобы не тянуть всю Event-сущность и не плодить JPA-связи между сервисами.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortInfoDto {
	private Long id;
	private Long initiatorId;
	private EventState state;
	private Long participantLimit;
	private Boolean requestModeration;
	private LocalDateTime publishedOn;
}

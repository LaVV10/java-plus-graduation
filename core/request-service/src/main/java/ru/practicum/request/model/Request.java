package ru.practicum.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.enums.RequestStatus;

import java.time.LocalDateTime;

/**
 * Заявка на участие. Event и User живут в других сервисах, поэтому хранятся только их id
 * (FK в общей схеме БД остаются для целостности, но JPA-связей к чужим сущностям нет).
 */
@Getter
@Setter
@Entity
@Table(name = "requests", schema = "public")
@AllArgsConstructor
@NoArgsConstructor
public class Request {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDateTime created;

	@Column(name = "event")
	private Long eventId;

	@Column(name = "requester")
	private Long requesterId;

	@Enumerated(EnumType.STRING)
	private RequestStatus status;
}

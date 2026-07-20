package ru.practicum.event.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.common.enums.EventState;

import java.time.LocalDateTime;

/**
 * Событие. Категория и инициатор живут в других сервисах, поэтому хранятся только их id
 * (FK в общей схеме БД остаются для целостности, но JPA-связей @OneToOne к Category/User НЕТ —
 * обогащение DTO делается через Feign в сервисном слое).
 *
 * Location остаётся как @OneToOne(cascade=ALL) — она принадлежит event-service.
 */
@Getter
@Setter
@Entity
@Table(name = "events", schema = "public")
@NoArgsConstructor
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String annotation;

	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "created_On")
	private LocalDateTime createdOn;
	private String description;

	private LocalDateTime eventDate;

	@Column(name = "initiator_id")
	private Long initiatorId;

	@OneToOne(cascade = {CascadeType.ALL})
	@JoinColumn(name = "location_id", referencedColumnName = "id")
	private Location location;
	private Boolean paid;
	private int participantLimit;
	private LocalDateTime publishedOn;
	private Boolean requestModeration;

	@Enumerated(EnumType.STRING)
	private EventState state;
	private String title;
}

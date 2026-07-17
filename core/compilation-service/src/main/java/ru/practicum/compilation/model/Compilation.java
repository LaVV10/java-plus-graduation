package ru.practicum.compilation.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Подборка событий. События хранятся только идентификаторами: Event живёт в event-service,
 * поэтому прямой JPA-связи (@ManyToMany) быть не может. Join-таблица compilations_events
 * маппится как @ElementCollection коллекции Long (event_id).
 *
 * Сами данные событий (для ответа) подтягиваются через Feign к event-service; сортировка
 * событий по дате выполняется в коде (по EventShortDto.eventDate), т.к. данные события
 * больше не лежат в этой БД.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "compilations", schema = "public")
public class Compilation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Boolean pinned;
	private String title;

	@ElementCollection
	@CollectionTable(
			name = "compilations_events",
			joinColumns = @JoinColumn(name = "compilation_id")
	)
	@Column(name = "event_id")
	private List<Long> eventIds = new ArrayList<>();
}

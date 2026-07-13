package ru.practicum.main.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.config.ConfigUtil;
import ru.practicum.main.enumeration.EventState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @OneToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "created_On")
    private LocalDateTime createdOn;
    private String description;

    private LocalDateTime eventDate;
    @OneToOne
    @JoinColumn(name = "initiator_id", referencedColumnName = "id")
    private User initiator;

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

    @Transient
    private final String datePattern = ConfigUtil.DATE;

    @Transient
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);
}
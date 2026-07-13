package ru.practicum.main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.practicum.main.enumeration.RequestStatus;

import java.time.LocalDateTime;

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

    @ManyToOne
    @JoinColumn(name = "event")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Event event;

    @ManyToOne
    @JoinColumn(name = "requester")
    @OnDelete(action = OnDeleteAction.CASCADE)
    User requester;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

}
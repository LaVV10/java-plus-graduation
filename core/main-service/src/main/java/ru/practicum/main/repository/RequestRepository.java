package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("SELECT r FROM Request r " + "WHERE r.event.initiator.id = :userId")
    List<Request> findByEventInitiatorId(@Param(value = "userId") Long userId);

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByRequesterId(Long userId);

    Optional<Request> findByRequesterIdAndId(Long userId, Long requestId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByEventInAndStatus(List<Event> events, RequestStatus status);

}
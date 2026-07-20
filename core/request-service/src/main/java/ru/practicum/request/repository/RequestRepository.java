package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.common.enums.RequestStatus;
import ru.practicum.request.model.Request;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

	Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

	List<Request> findAllByRequesterId(Long userId);

	Optional<Request> findByRequesterIdAndId(Long userId, Long requestId);

	Integer countByEventIdAndStatus(Long eventId, RequestStatus status);

	List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

	List<Request> findByEventId(Long eventId);

}

package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
}

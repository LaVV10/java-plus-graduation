package ru.practicum.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.feature.model.Compilation;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}

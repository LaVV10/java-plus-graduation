package ru.practicum.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.feature.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	Boolean existsByName(String name);
}

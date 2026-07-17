package ru.practicum.category.service;

import ru.practicum.category.model.Category;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
	CategoryDto createCategory(NewCategoryDto newCategoryDto);

	List<CategoryDto> getCategories(Integer from, Integer size);

	CategoryDto getCategory(Long catId);

	void deleteCategory(Long catId);

	CategoryDto updateCategory(Long catId, CategoryDto categoryDto);

	/** Внутренний доступ к модели по id (для /internal API). */
	Category getCategoryModelById(Long catId);

	/** Внутренний batch-доступ по id (для /internal API). */
	List<Category> getCategoriesByIds(List<Long> categoriesIds);
}

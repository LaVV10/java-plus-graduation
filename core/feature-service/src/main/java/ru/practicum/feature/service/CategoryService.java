package ru.practicum.feature.service;

import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
	CategoryDto createCategory(NewCategoryDto newCategoryDto);

	List<CategoryDto> getCategories(Integer from, Integer size);

	CategoryDto getCategory(Long catId);

	void deleteCategory(Long catId);

	CategoryDto updateCategory(Long catId, CategoryDto categoryDto);
}

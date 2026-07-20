package ru.practicum.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.category.model.Category;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

@Component
public class CategoryMapper {

	public Category toCategory(NewCategoryDto newCategoryDto) {
		if (newCategoryDto == null) {
			return null;
		}

		Category category = new Category();
		category.setName(newCategoryDto.getName());
		return category;
	}

	public CategoryDto toCategoryDto(Category category) {
		if (category == null) {
			return null;
		}

		CategoryDto categoryDto = new CategoryDto();
		categoryDto.setId(category.getId());
		categoryDto.setName(category.getName());
		return categoryDto;
	}
}

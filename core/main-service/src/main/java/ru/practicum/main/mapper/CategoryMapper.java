package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.model.Category;

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
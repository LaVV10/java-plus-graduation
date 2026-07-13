package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.main.dto.category.CategoryDto;
import ru.practicum.main.dto.category.NewCategoryDto;
import ru.practicum.main.exception.CategoryNotExistException;
import ru.practicum.main.exception.NameAlreadyExistException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new NameAlreadyExistException(String.format("Can't create category because name: %s already used by another category", newCategoryDto.getName()));
        }
        return categoryMapper.toCategoryDto(categoryRepository.save(categoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        List<Category> allCategories = categoryRepository.findAll(Sort.by("id"));

        return allCategories.stream()
                .skip(from)
                .limit(size)
                .map(categoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotExistException("Category doesn't exist"));
        return categoryMapper.toCategoryDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        categoryRepository.deleteById(catId);
    }


    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotExistException("Category doesn't exist"));
        if (categoryRepository.existsByName(categoryDto.getName()) && !categoryDto.getName().equals(category.getName())) {
            throw new NameAlreadyExistException(String.format("Can't update category because name: %s already used by another category", categoryDto.getName()));
        }
        category.setName(categoryDto.getName());
        return categoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public Category getCategoryModelById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new CategoryNotExistException(
                        String.format("Category with id=%s was not found", catId)));
    }

    @Override
    public List<Category> getCategoriesByIds(List<Long> categoriesIds) {
        return categoryRepository.findAllById(categoriesIds);
    }
}
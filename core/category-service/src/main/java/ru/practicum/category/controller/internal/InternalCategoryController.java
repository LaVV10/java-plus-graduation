package ru.practicum.category.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.common.dto.CategoryDto;

import java.util.List;

/**
 * Внутреннее API category-service для межсервисного взаимодействия (Feign) с event-service.
 * НЕ маршрутизируется gateway-ем наружу — префикс /internal исключён из публичных маршрутов.
 *
 * Контракт:
 *   GET /internal/categories/{id}      -> CategoryDto (404 если не найдена)
 *   GET /internal/categories?ids=1,2   -> List<CategoryDto>
 */
@RestController
@RequestMapping("/internal/categories")
@RequiredArgsConstructor
public class InternalCategoryController {

	private final CategoryService categoryService;
	private final CategoryMapper categoryMapper;

	@GetMapping("/{id}")
	public CategoryDto getCategoryById(@PathVariable Long id) {
		Category category = categoryService.getCategoryModelById(id);
		return categoryMapper.toCategoryDto(category);
	}

	@GetMapping
	public List<CategoryDto> getCategoriesByIds(@RequestParam("ids") List<Long> ids) {
		List<Category> categories = categoryService.getCategoriesByIds(ids);
		return categories.stream()
				.map(categoryMapper::toCategoryDto)
				.toList();
	}
}

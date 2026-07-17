package ru.practicum.feature.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.feature.mapper.CategoryMapper;
import ru.practicum.feature.model.Category;
import ru.practicum.feature.service.impl.CategoryServiceImpl;

import java.util.List;

/**
 * Внутреннее API feature-service для межсервисного взаимодействия (Feign) с event-service.
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

	private final CategoryServiceImpl categoryService;
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

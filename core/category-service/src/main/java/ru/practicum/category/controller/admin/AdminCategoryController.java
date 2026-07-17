package ru.practicum.category.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.client.EventClient;
import ru.practicum.category.exception.CategoryNotEmptyException;
import ru.practicum.category.service.CategoryService;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.common.dto.NewCategoryDto;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Validated
public class AdminCategoryController {
	private final CategoryService categoryService;
	private final EventClient eventClient;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
		return categoryService.createCategory(newCategoryDto);
	}

	@DeleteMapping("/{catId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteCategory(@PathVariable Long catId) {
		// Перед удалением категории убеждаемся через event-service, что к ней не привязано событий.
		if (Boolean.TRUE.equals(eventClient.existsByCategoryId(catId))) {
			throw new CategoryNotEmptyException("Category is not empty");
		}
		categoryService.deleteCategory(catId);
	}

	@PatchMapping("/{catId}")
	public CategoryDto updateCategory(@PathVariable Long catId, @RequestBody @Valid CategoryDto categoryDto) {
		return categoryService.updateCategory(catId, categoryDto);
	}
}

package ru.practicum.category.controller.publicAccess;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.service.CategoryService;
import ru.practicum.common.dto.CategoryDto;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@RequiredArgsConstructor
public class PublicCategoryController {
	private final CategoryService categoryService;

	@GetMapping
	public List<CategoryDto> getCategories(@RequestParam(required = false, defaultValue = "0") Integer from,
										   @RequestParam(required = false, defaultValue = "10") Integer size) {
		return categoryService.getCategories(from, size);
	}

	@GetMapping("/{catId}")
	public CategoryDto getCategory(@PathVariable Long catId) {
		return categoryService.getCategory(catId);
	}
}

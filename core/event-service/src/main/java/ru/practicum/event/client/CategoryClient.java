package ru.practicum.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.event.client.fallback.CategoryClientFallback;

import java.util.List;

/**
 * Feign-клиент к category-service для домена event-service.
 * Получает данные категорий для обогащения EventFullDto/EventShortDto.
 *
 * Fallback @{@link CategoryClientFallback}: при недоступности category-service категория
 * возвращается как null (в DTO поле category будет отсутствовать), а список — пустой.
 */
@FeignClient(name = "category-service", fallback = CategoryClientFallback.class)
public interface CategoryClient {

	@GetMapping("/internal/categories/{id}")
	CategoryDto getCategoryById(@PathVariable("id") Long categoryId);

	@GetMapping("/internal/categories")
	List<CategoryDto> getCategoriesByIds(@RequestParam("ids") List<Long> ids);
}

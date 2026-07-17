package ru.practicum.event.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.common.dto.CategoryDto;
import ru.practicum.event.client.CategoryClient;

import java.util.List;

/**
 * Fallback для {@link CategoryClient}: при недоступности feature-service категория = null,
 * список = пустой. Публичная выдача событий продолжает работать без category.
 */
@Slf4j
@Component
public class CategoryClientFallback implements CategoryClient {

	@Override
	public CategoryDto getCategoryById(Long categoryId) {
		log.warn("Fallback getCategoryById({}): feature-service недоступен, возвращаем null", categoryId);
		return null;
	}

	@Override
	public List<CategoryDto> getCategoriesByIds(List<Long> ids) {
		log.warn("Fallback getCategoriesByIds({}): feature-service недоступен, возвращаем пустой список", ids);
		return List.of();
	}
}

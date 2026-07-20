package ru.practicum.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Микросервис категорий (category-service).
 *
 * Домен: категории событий (categories). Исходящий Feign-вызов — в event-service
 * (проверка, что категория не привязана к событиям, перед удалением).
 * Экспортирует внутреннее API /internal/categories для event-service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CategoryServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CategoryServiceApplication.class, args);
	}
}

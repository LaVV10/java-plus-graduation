package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstrap-оболочка основного сервиса (Этап 2).
 *
 * После разделения на микросервисы (event/request/user/category/compilation-service) здесь практически
 * не осталось исходного кода — вся бизнес-логика перенесена в выделенные микросервисы.
 * Этот модуль сохранён как точка входа основного сервиса и зарезервирован под будущие
 * общие задачи (например, агрегирующие эндпоинты). Трафик маршрутизируется gateway-ом
 * напрямую в микросервисы, поэтому main-service в docker-compose не поднимается.
 */
@SpringBootApplication
public class MainServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MainServiceApplication.class, args);
	}
}

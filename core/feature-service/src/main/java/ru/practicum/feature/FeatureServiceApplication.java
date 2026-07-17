package ru.practicum.feature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Микросервис «дополнительной функциональности»: категории (categories) и подборки (compilations).
 *
 * Исходящие Feign-вызовы: в event-service (проверка использования категории, получение событий
 * подборки). Экспортирует внутреннее API /internal/categories для event-service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class FeatureServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(FeatureServiceApplication.class, args);
	}
}

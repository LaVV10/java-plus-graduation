package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Микросервис заявок на участие (requests).
 *
 * Исходящие Feign-вызовы: в event-service (снимок события для модерации заявки) и
 * user-service (валидация существования пользователя). Экспортирует внутреннее API
 * /internal/requests (подсчёт подтверждённых заявок) для event-service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class RequestServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RequestServiceApplication.class, args);
	}
}

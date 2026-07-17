package ru.practicum.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Микросервис пользователей (users).
 *
 * Листовой домен: не делает исходящих межсервисных вызовов, но Feign включён для
 * симметрии инфраструктуры и возможных будущих потребителей. Экспортирует внутреннее
 * API /internal/users для event-service и request-service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}

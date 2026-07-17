package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 * Микросервис мероприятий (events + locations) — центральный домен.
 *
 * Исходящие Feign-вызовы: feature-service (категории), user-service (инициаторы),
 * request-service (подсчёт подтверждённых заявок), stats-server (просмотры, через stats-client).
 * Экспортирует внутреннее API /internal/events для request-service и feature-service.
 *
 * ComponentScan по ru.practicum нужен, чтобы подхватить StatsClient (лежит в ru.practicum.stats.client).
 * RestTemplate @{@link LoadBalanced} нужен stats-client (он построен на RestTemplate).
 */
@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum")
@EnableDiscoveryClient
@EnableFeignClients
public class EventServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EventServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}

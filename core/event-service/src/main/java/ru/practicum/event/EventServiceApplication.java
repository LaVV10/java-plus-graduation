package ru.practicum.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Микросервис мероприятий (events + locations) — центральный домен.
 *
 * <p>Исходящие вызовы:
 * <ul>
 *   <li>Feign: category-service (категории), user-service (инициаторы),
 *       request-service (подсчёт подтверждённых заявок);</li>
 *   <li>gRPC: Collector (отправка действий VIEW/REGISTER/LIKE) и Analyzer
 *       (запрос рекомендаций и рейтинга) — через модуль stats-client.</li>
 * </ul>
 * Экспортирует внутреннее API /internal/events для request-service и compilation-service.
 *
 * <p>{@code ComponentScan} по {@code ru.practicum} нужен, чтобы подхватить gRPC-клиенты
 * CollectorClient/AnalyzerClient (лежат в {@code ru.practicum.stats.client}). Адреса
 * gRPC-сервисов резолвятся через Eureka ({@code discovery:///collector},
 * {@code discovery:///analyzer}) — настраивается в {@code config_repo/event-service.yml}.
 */
@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum")
@EnableDiscoveryClient
@EnableFeignClients
public class EventServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(EventServiceApplication.class, args);
	}
}

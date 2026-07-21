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
 * CollectorClient/AnalyzerClient (лежат в {@code ru.practicum.stats.client}). При сканировании
 * подхватывается и старый {@link ru.practicum.stats.client.StatsClient} (HTTP, не используется,
 * но остаётся в classpath stats-client) — для него требуется бин {@code RestTemplate}.
 * Адреса gRPC-сервисов резолвятся через Eureka ({@code discovery:///collector},
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

	/**
	 * {@code @LoadBalanced} {@link RestTemplate} для резолва имён сервисов через Eureka.
	 * Нужен бину {@link ru.practicum.stats.client.StatsClient} из модуля stats-client
	 * (подхватывается широким ComponentScan).
	 */
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}


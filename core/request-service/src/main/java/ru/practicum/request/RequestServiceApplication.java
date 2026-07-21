package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

/**
 * Микросервис заявок на участие (requests).
 *
 * <p>Исходящие вызовы:
 * <ul>
 *   <li>Feign: в event-service (снимок события для модерации заявки) и user-service
 *       (валидация существования пользователя);</li>
 *   <li>gRPC: в Collector (отправка ACTION_REGISTER при создании заявки) через
 *       модуль stats-client.</li>
 * </ul>
 * Экспортирует внутреннее API /internal/requests (подсчёт подтверждённых заявок) для event-service.
 *
 * <p>{@code ComponentScan} по {@code ru.practicum} нужен, чтобы подхватить gRPC-клиент
 * CollectorClient (лежит в {@code ru.practicum.stats.client}). При сканировании подхватывается
 * и старый {@link ru.practicum.stats.client.StatsClient} (HTTP, не используется, но остаётся в
 * classpath stats-client) — для него требуется бин {@code RestTemplate}.
 * Адрес Collector резолвится через Eureka ({@code discovery:///collector}) — настраивается
 * в {@code config_repo/request-service.yml}.
 */
@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum")
@EnableDiscoveryClient
@EnableFeignClients
public class RequestServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RequestServiceApplication.class, args);
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


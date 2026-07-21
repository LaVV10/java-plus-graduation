package ru.practicum.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

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
 * CollectorClient (лежит в {@code ru.practicum.stats.client}). Адрес Collector резолвится
 * через Eureka ({@code discovery:///collector}) — настраивается в {@code config_repo/request-service.yml}.
 */
@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum")
@EnableDiscoveryClient
@EnableFeignClients
public class RequestServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RequestServiceApplication.class, args);
	}
}

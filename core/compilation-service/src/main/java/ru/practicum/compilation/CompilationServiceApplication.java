package ru.practicum.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Микросервис подборок событий (compilation-service).
 *
 * Домен: подборки (compilations). Подборка хранит только id событий — сами Event живут
 * в event-service, поэтому данные событий для ответа подтягиваются через Feign.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CompilationServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(CompilationServiceApplication.class, args);
	}
}

package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Analyzer.
 *
 * Читает сходства и действия из Kafka в БД и по gRPC отдаёт рекомендации
 * мероприятий по трём алгоритмам.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerApplication.class, args);
	}
}

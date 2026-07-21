package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Analyzer (Этап 3-2).
 *
 * Читает {@link ru.practicum.ewm.stats.avro.EventSimilarityAvro} и
 * {@link ru.practicum.ewm.stats.avro.UserActionAvro} из Kafka в БД, и по gRPC
 * (RecommendationsController) отдаёт рекомендации мероприятий по трём алгоритмам:
 * <ul>
 *   <li>{@code GetRecommendationsForUser} — предсказание оценки;</li>
 *   <li>{@code GetSimilarEvents} — похожие мероприятия;</li>
 *   <li>{@code GetInteractionsCount} — сумма максимальных весов взаимодействий.</li>
 * </ul>
 * Регистрируется в Eureka как {@code analyzer}; gRPC-сервер на случайном порту.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyzerApplication.class, args);
	}
}

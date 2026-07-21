package ru.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Aggregator (Этап 3-2).
 *
 * Потоково читает {@link ru.practicum.ewm.stats.avro.UserActionAvro} из Kafka-топика
 * {@code stats.user-actions.v1}, инкрементально пересчитывает косинусное сходство мероприятий
 * (см. {@link ru.practicum.aggregator.similarity.SimilarityCalculator}) и отправляет
 * {@link ru.practicum.ewm.stats.avro.EventSimilarityAvro} в топик
 * {@code stats.events-similarity.v1}.
 *
 * Реализован на Kafka Streams с тремя state stores (cм. {@code AggregatorStreamsConfig}).
 * Регистрируется в Eureka как {@code aggregator}; конфигурацию тянет из Config Server.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AggregatorApplication.class, args);
	}
}

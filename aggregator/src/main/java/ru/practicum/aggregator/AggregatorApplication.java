package ru.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Aggregator.
 *
 * Читает действия пользователей из Kafka, инкрементально пересчитывает косинусное
 * сходство мероприятий и отправляет обновления в Kafka-топик сходств.
 * Реализован на Kafka Streams.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(AggregatorApplication.class, args);
	}
}

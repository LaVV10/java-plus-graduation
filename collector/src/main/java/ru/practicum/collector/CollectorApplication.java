package ru.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Collector.
 *
 * Принимает действия пользователей по gRPC и отправляет их в Kafka-топик
 * {@code stats.user-actions.v1} в Avro-формате.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}
}

package ru.practicum.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Сервис Collector (Этап 3-2).
 *
 * Принимает действия пользователей по gRPC (UserActionController.CollectUserAction)
 * и асинхронно отправляет их в Kafka-топик {@code stats.user-actions.v1}
 * в Avro-формате ({@link ru.practicum.ewm.stats.avro.UserActionAvro}).
 *
 * Регистрируется в Eureka как {@code collector}; конфигурацию тянет из Config Server.
 * gRPC-сервер стартует на случайном порту ({@code grpc.server.port=0}).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollectorApplication.class, args);
	}
}

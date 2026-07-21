package ru.practicum.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Настройка Kafka consumers для топиков {@code stats.user-actions.v1} и
 * {@code stats.events-similarity.v1}. Значения — чистый Avro (без Confluent magic byte),
 * десериализуются через {@link AvroDeserializer}.
 *
 * <p>Ключевой приём (как в референс-решении Danny1kk): group.id генерируется с
 * UUID-суффиксом на каждый запуск + при старте offset перематывается в конец топика
 * через {@code seekToEnd}. Это гарантирует, что новый запуск не унаследует offset'ы
 * прошлых прогонов и не прочтёт старые битые сообщения из топика (актуально в CI
 * Практикума, где Kafka-топики персистентны между тестами).
 */
@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	private Map<String, Object> baseProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		// UUID в group.id → новая consumer-группа каждый запуск → нет привязки к старым offset'ам.
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-" + UUID.randomUUID());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return props;
	}

	@Bean(name = "userActionConsumerFactory")
	public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(
				baseProps(), new StringDeserializer(), new AvroDeserializer<>(UserActionAvro.class));
	}

	@Bean(name = "userActionContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionContainerFactory(
			ConsumerFactory<String, UserActionAvro> userActionConsumerFactory) {
		return buildFactory(userActionConsumerFactory);
	}

	@Bean(name = "eventSimilarityConsumerFactory")
	public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(
				baseProps(), new StringDeserializer(), new AvroDeserializer<>(EventSimilarityAvro.class));
	}

	@Bean(name = "eventSimilarityContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, EventSimilarityAvro> eventSimilarityContainerFactory(
			ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory) {
		return buildFactory(eventSimilarityConsumerFactory);
	}

	private <V> ConcurrentKafkaListenerContainerFactory<String, V> buildFactory(
			ConsumerFactory<String, V> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, V> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		return factory;
	}

	/**
	 * При старте перематывает offset обоих топиков в конец — пропускает все накопленные
	 * (потенциально битые от прошлых прогонов) сообщения. Новый consumer начнёт чтение
	 * только с сообщений, появившихся после старта сервиса.
	 */
	@Bean
	public ApplicationRunner seekToEndOnStartup(
			ConsumerFactory<String, UserActionAvro> userActionConsumerFactory,
			ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory) {
		return args -> {
			seekToEnd(userActionConsumerFactory, "stats.user-actions.v1", "analyzer-reset-ua");
			seekToEnd(eventSimilarityConsumerFactory, "stats.events-similarity.v1", "analyzer-reset-es");
		};
	}

	private <V> void seekToEnd(ConsumerFactory<String, V> factory, String topic, String resetClientId) {
		try (var consumer = factory.createConsumer(resetClientId, "reset")) {
			var partitions = consumer.partitionsFor(topic).stream()
					.map(p -> new TopicPartition(p.topic(), p.partition()))
					.collect(Collectors.toList());
			if (partitions.isEmpty()) {
				return;
			}
			consumer.assign(partitions);
			consumer.seekToEnd(partitions);
			consumer.commitSync();
			log.info("Analyzer consumer offset перемотан в конец топика {}", topic);
		} catch (Exception e) {
			log.warn("Не удалось перемотать offset топика {} (не критично): {}", topic, e.getMessage());
		}
	}
}

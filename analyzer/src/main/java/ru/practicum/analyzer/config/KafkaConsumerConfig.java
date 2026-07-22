package ru.practicum.analyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
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
 * Настройка Kafka consumers для топиков действий и сходств.
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
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "analyzer-" + UUID.randomUUID());
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return props;
	}

	@Bean(name = "userActionConsumerFactory")
	public ConsumerFactory<Long, UserActionAvro> userActionConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(
				baseProps(), new LongDeserializer(), new AvroDeserializer<>(UserActionAvro.class));
	}

	@Bean(name = "userActionContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<Long, UserActionAvro> userActionContainerFactory(
			ConsumerFactory<Long, UserActionAvro> userActionConsumerFactory) {
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

	private <K, V> ConcurrentKafkaListenerContainerFactory<K, V> buildFactory(
			ConsumerFactory<K, V> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<K, V> factory =
				new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory);
		factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
		return factory;
	}

	/**
	 * При старте перематывает offset в конец топиков, чтобы читать только новые сообщения.
	 */
	@Bean
	public ApplicationRunner seekToEndOnStartup(
			ConsumerFactory<Long, UserActionAvro> userActionConsumerFactory,
			ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory) {
		return args -> {
			seekToEnd(userActionConsumerFactory, "stats.user-actions.v1", "analyzer-reset-ua");
			seekToEnd(eventSimilarityConsumerFactory, "stats.events-similarity.v1", "analyzer-reset-es");
		};
	}

	private <K, V> void seekToEnd(ConsumerFactory<K, V> factory, String topic, String resetClientId) {
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
			log.info("Offset перемотан в конец топика {}", topic);
		} catch (Exception e) {
			log.warn("Не удалось перемотать offset топика {} (не критично): {}", topic, e.getMessage());
		}
	}
}

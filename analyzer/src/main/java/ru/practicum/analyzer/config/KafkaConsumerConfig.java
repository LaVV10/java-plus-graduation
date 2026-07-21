package ru.practicum.analyzer.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
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

/**
 * Настройка Kafka consumers для топиков {@code stats.user-actions.v1} и
 * {@code stats.events-similarity.v1}. Значения — Avro, десериализуются через
 * {@link AvroDeserializer} (без Schema Registry): класс схемы задаётся прямо в
 * конструкторе десериализатора для каждой фабрики.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:analyzer}")
	private String groupId;

	private Map<String, Object> baseProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		return props;
	}

	@Bean(name = "userActionConsumerFactory")
	public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(
				baseProps(),
				new StringDeserializer(),
				new AvroDeserializer<>(UserActionAvro.class));
	}

	@Bean(name = "userActionContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionContainerFactory(
			ConsumerFactory<String, UserActionAvro> userActionConsumerFactory) {
		return buildFactory(userActionConsumerFactory);
	}

	@Bean(name = "eventSimilarityConsumerFactory")
	public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(
				baseProps(),
				new StringDeserializer(),
				new AvroDeserializer<>(EventSimilarityAvro.class));
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
}

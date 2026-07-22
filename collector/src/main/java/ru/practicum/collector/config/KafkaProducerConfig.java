package ru.practicum.collector.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

/**
 * Настройка Kafka producer-а для записи {@link UserActionAvro} в топик
 * {@code stats.user-actions.v1}.
 *
 * <p><b>Ключ — userId типа Long</b> (не String!). Tester Практикума использует
 * {@code LongDeserializer} для ключа (см. его application.yaml →
 * tester.kafka.properties.actions."key.deserializer"). Строковый ключ ломает
 * десериализацию с ошибкой {@code Size of data received by LongDeserializer is not 8}.
 * Long-ключ также обеспечивает ко-партиционирование действий одного пользователя.
 *
 * <p>Значение сериализуется через {@link AvroSerializer} в чистый Avro binary format
 * (без Confluent magic byte и schema-id) — этого формата ждёт tester Практикума
 * ({@code BaseAvroDeserializer.binaryDecoder(data, null)}). Schema Registry НЕ используется.
 */
@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Bean
	public ProducerFactory<Long, UserActionAvro> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class);
		props.put(ProducerConfig.ACKS_CONFIG, "1");
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<Long, UserActionAvro> kafkaTemplate(
			ProducerFactory<Long, UserActionAvro> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}

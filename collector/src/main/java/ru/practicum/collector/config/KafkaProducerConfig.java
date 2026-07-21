package ru.practicum.collector.config;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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
 * <p>Ключ — строковое представление userId (гарантирует ко-партиционирование действий
 * одного пользователя). Значение сериализуется через Confluent {@link KafkaAvroSerializer}
 * (wire format: magic byte 0 + schema-id + Avro-payload) — этого формата ждут tester
 * Практикума, Aggregator и Analyzer. Схема регистрируется в Schema Registry автоматически
 * при первом {@code send()}.
 */
@Configuration
public class KafkaProducerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.properties.schema.registry.url}")
	private String schemaRegistryUrl;

	@Bean
	public ProducerFactory<String, UserActionAvro> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
		// Confluent: адрес Schema Registry. Без него KafkaAvroSerializer не стартует.
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
		// Регистрируем схему автоматически (по умолчанию true, но явно для ясности).
		props.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, true);
		props.put(ProducerConfig.ACKS_CONFIG, "1");
		props.put(ProducerConfig.RETRIES_CONFIG, 3);
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, UserActionAvro> kafkaTemplate(
			ProducerFactory<String, UserActionAvro> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}
}

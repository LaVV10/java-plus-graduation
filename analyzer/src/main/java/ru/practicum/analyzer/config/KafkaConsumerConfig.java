package ru.practicum.analyzer.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
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
 * {@code stats.events-similarity.v1}. Значения — Avro в Confluent wire format
 * (magic byte 0 + schema-id + payload), десериализуются через {@link KafkaAvroDeserializer}
 * со Schema Registry.
 *
 * <p>{@code specific.avro.reader=true} — чтобы десериализатор возвращал типизированные
 * классы ({@link UserActionAvro}, {@link EventSimilarityAvro}), а не {@code GenericRecord}.
 * Класс схемы указывается в {@code DefaultKafkaConsumerFactory} третьим аргументом
 * (value deserializer) — создаётся отдельная фабрика под каждый класс.
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value("${spring.kafka.consumer.group-id:analyzer}")
	private String groupId;

	@Value("${app.kafka.schema-registry-url:${SCHEMA_REGISTRY_URL:http://localhost:8081}}")
	private String schemaRegistryUrl;

	private Map<String, Object> baseProps() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		// Confluent Avro
		props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
		props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
		return props;
	}

	/**
	 * Confluent Avro-десериализатор с {@code specific.avro.reader=true}: возвращает
	 * {@link SpecificRecord} (общий предок Avro-классов), конкретный класс определяется
	 * по схеме payload. В точках использования тип сужается через raw-приведение.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private Deserializer<SpecificRecord> avroDeserializer() {
		// KafkaAvroDeserializer implements Deserializer<Object>, а не Deserializer<SpecificRecord> —
		// прямое присвоение не проходит. Приводим через raw-тип (в рантайме generics стираются).
		KafkaAvroDeserializer rawDeserializer = new KafkaAvroDeserializer();
		rawDeserializer.configure(baseProps(), false);
		return (Deserializer<SpecificRecord>) (Deserializer) rawDeserializer;
	}

	@Bean(name = "userActionConsumerFactory")
	@SuppressWarnings({"rawtypes", "unchecked"})
	public ConsumerFactory<String, UserActionAvro> userActionConsumerFactory() {
		Deserializer deserializer = avroDeserializer();
		return new DefaultKafkaConsumerFactory<>(
				baseProps(), new StringDeserializer(), deserializer);
	}

	@Bean(name = "userActionContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, UserActionAvro> userActionContainerFactory(
			ConsumerFactory<String, UserActionAvro> userActionConsumerFactory) {
		return buildFactory(userActionConsumerFactory);
	}

	@Bean(name = "eventSimilarityConsumerFactory")
	@SuppressWarnings({"rawtypes", "unchecked"})
	public ConsumerFactory<String, EventSimilarityAvro> eventSimilarityConsumerFactory() {
		Deserializer deserializer = avroDeserializer();
		return new DefaultKafkaConsumerFactory<>(
				baseProps(), new StringDeserializer(), deserializer);
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

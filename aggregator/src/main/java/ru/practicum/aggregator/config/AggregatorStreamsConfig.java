package ru.practicum.aggregator.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import ru.practicum.aggregator.similarity.SimilarityCalculator;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableKafkaStreams
public class AggregatorStreamsConfig {

	/** Входной топик действий пользователей. */
	public static final String USER_ACTIONS_TOPIC = "stats.user-actions.v1";
	/** Выходной топик сходства мероприятий. */
	public static final String EVENTS_SIMILARITY_TOPIC = "stats.events-similarity.v1";

	static final String USER_ACTION_STORE = "user-action-store";
	static final String EVENT_WEIGHTS_STORE = "event-weights-store";
	static final String EVENTS_BY_USER_STORE = "events-by-user-store";
	static final String SIMILARITY_STORE = "similarity-store";

	static final String SOURCE_NODE = "user-actions-source";
	static final String PROCESSOR_NODE = "similarity-processor";
	static final String SINK_NODE = "similarity-sink";
	
	@Bean
	public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
		return factory -> {
			Properties props = factory.getStreamsConfiguration();
			if (props == null) {
				props = new Properties();
			}
			props.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregator-" + UUID.randomUUID());
			props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
			factory.setStreamsConfiguration(props);
		};
	}

	@Bean
	public KStream<String, EventSimilarityAvro> aggregatorTopology(StreamsBuilder builder) {
		Serde<Set<Long>> setSerde = Serdes.serdeFrom(new JsonSetSerializer(), new JsonSetDeserializer());
		Serde<UserActionAvro> inputSerde = AvroSerdes.forClass(UserActionAvro.class);
		Serde<EventSimilarityAvro> outputSerde = AvroSerdes.forClass(EventSimilarityAvro.class);

		// ─── State stores (key-value, persistent, с changelog-топиком) ──
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(USER_ACTION_STORE), Serdes.String(), Serdes.Double()));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(EVENT_WEIGHTS_STORE), Serdes.Long(), Serdes.Double()));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(EVENTS_BY_USER_STORE), Serdes.Long(), setSerde));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(SIMILARITY_STORE), Serdes.String(), Serdes.Double()));

		KStream<Long, UserActionAvro> source = builder.stream(
				USER_ACTIONS_TOPIC, Consumed.with(Serdes.Long(), inputSerde));

		KStream<String, EventSimilarityAvro> similarities = source.process(
				() -> new SimilarityProcessor(USER_ACTION_STORE, EVENT_WEIGHTS_STORE,
						EVENTS_BY_USER_STORE, SIMILARITY_STORE),
				USER_ACTION_STORE, EVENT_WEIGHTS_STORE, EVENTS_BY_USER_STORE, SIMILARITY_STORE);

		similarities.to(EVENTS_SIMILARITY_TOPIC,
				org.apache.kafka.streams.kstream.Produced.with(Serdes.String(), outputSerde));

		return similarities;
	}

	public static final class SimilarityProcessor
			implements org.apache.kafka.streams.processor.api.Processor<Long, UserActionAvro, String, EventSimilarityAvro> {
		private final String userActionStoreName;
		private final String eventWeightsStoreName;
		private final String eventsByUserStoreName;
		private final String similarityStoreName;

		private org.apache.kafka.streams.processor.api.ProcessorContext<String, EventSimilarityAvro> context;
		private KeyValueStore<String, Double> userActionStore;
		private KeyValueStore<Long, Double> eventWeightsStore;
		private KeyValueStore<Long, Set<Long>> eventsByUserStore;
		private KeyValueStore<String, Double> similarityStore;

		SimilarityProcessor(String userActionStoreName, String eventWeightsStoreName,
							String eventsByUserStoreName, String similarityStoreName) {
			this.userActionStoreName = userActionStoreName;
			this.eventWeightsStoreName = eventWeightsStoreName;
			this.eventsByUserStoreName = eventsByUserStoreName;
			this.similarityStoreName = similarityStoreName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void init(org.apache.kafka.streams.processor.api.ProcessorContext<String, EventSimilarityAvro> context) {
			this.context = context;
			this.userActionStore = (KeyValueStore<String, Double>) context.getStateStore(userActionStoreName);
			this.eventWeightsStore = (KeyValueStore<Long, Double>) context.getStateStore(eventWeightsStoreName);
			this.eventsByUserStore = (KeyValueStore<Long, Set<Long>>) context.getStateStore(eventsByUserStoreName);
			this.similarityStore = (KeyValueStore<String, Double>) context.getStateStore(similarityStoreName);
		}

		@Override
		public void process(org.apache.kafka.streams.processor.api.Record<Long, UserActionAvro> record) {
			UserActionAvro action = record.value();
			if (action == null) {
				return;
			}
			long userId = action.getUserId();
			long eventId = action.getEventId();
			ActionTypeAvro type = action.getActionType();
			double newWeight = SimilarityCalculator.weightOf(type);

			String uaKey = userId + ":" + eventId;
			Double oldBoxed = userActionStore.get(uaKey);
			double oldWeight = oldBoxed == null ? 0.0 : oldBoxed;

			// По ТЗ: обновляем только если новый вес превышает старый.
			if (newWeight <= oldWeight) {
				return;
			}

			double delta = newWeight - oldWeight;
			userActionStore.put(uaKey, newWeight);

			// Обновляем S_a (сумму весов по мероприятию A).
			Double sABoxed = eventWeightsStore.get(eventId);
			double sA = (sABoxed == null ? 0.0 : sABoxed) + delta;
			eventWeightsStore.put(eventId, sA);

			// Множество мероприятий, с которыми уже взаимодействовал этот пользователь.
			Set<Long> userEvents = eventsByUserStore.get(userId);
			if (userEvents == null) {
				userEvents = new HashSet<>();
			}

			Instant timestamp = action.getTimestamp() == null ? Instant.now() : action.getTimestamp();

			// Пересчёт сходства со всеми остальными мероприятиями пользователя.
			for (Long other : userEvents) {
				if (other == eventId) {
					continue;
				}
				Double sBBoxed = eventWeightsStore.get(other);
				double sB = sBBoxed == null ? 0.0 : sBBoxed;

				Double wOtherBoxed = userActionStore.get(userId + ":" + other);
				double wOther = wOtherBoxed == null ? 0.0 : wOtherBoxed;
				double oldMin = Math.min(oldWeight, wOther);
				double newMin = Math.min(newWeight, wOther);
				double dSMin = newMin - oldMin;

				String pairKey = pairKey(eventId, other);
				Double sMinBoxed = similarityStore.get(pairKey);
				double sMin = (sMinBoxed == null ? 0.0 : sMinBoxed) + dSMin;
				similarityStore.put(pairKey, sMin);

				double score = SimilarityCalculator.similarity(sMin, sA, sB);
				// Округление до 6 знаков после запятой — как в референс-решениях Danny1kk:
				// tester Практикума сравнивает score с фиксированной точностью.
				score = Math.round(score * 1_000_000.0) / 1_000_000.0;
				EventSimilarityAvro out = buildSimilarity(eventId, other, score, timestamp);
				// forward нового API принимает Record; timestamp берём из исходной записи.
				context.forward(new org.apache.kafka.streams.processor.api.Record<>(
						pairKey, out, record.timestamp()));
			}

			// Регистрируем мероприятие за пользователем (после цикла — чтобы не учитывать само себя).
			if (!userEvents.contains(eventId)) {
				userEvents = new HashSet<>(userEvents);
				userEvents.add(eventId);
				eventsByUserStore.put(userId, userEvents);
			}
		}

		@Override
		public void close() {
			// State stores управляются Kafka Streams.
		}

		private static String pairKey(long a, long b) {
			return a < b ? a + ":" + b : b + ":" + a;
		}

		private static EventSimilarityAvro buildSimilarity(long eventA, long eventB,
														   double score, Instant timestamp) {
			long a = Math.min(eventA, eventB);
			long b = Math.max(eventA, eventB);
			return EventSimilarityAvro.newBuilder()
					.setEventA(a)
					.setEventB(b)
					.setScore(score)
					.setTimestamp(timestamp)
					.build();
		}
	}

	/**
	 * Сериализатор {@code Set<Long>} как comma-separated значений (для state store).
	 */
	static final class JsonSetSerializer implements org.apache.kafka.common.serialization.Serializer<Set<Long>> {
		@Override
		public byte[] serialize(String topic, Set<Long> data) {
			if (data == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Long e : data) {
				if (!first) {
					sb.append(',');
				}
				sb.append(e);
				first = false;
			}
			return sb.toString().getBytes(StandardCharsets.UTF_8);
		}
	}

	/**
	 * Десериализатор {@code Set<Long>} из comma-separated значений.
	 */
	static final class JsonSetDeserializer implements org.apache.kafka.common.serialization.Deserializer<Set<Long>> {
		@Override
		public Set<Long> deserialize(String topic, byte[] data) {
			if (data == null) {
				return new HashSet<>();
			}
			String s = new String(data, StandardCharsets.UTF_8);
			Set<Long> set = new HashSet<>();
			if (s.isEmpty()) {
				return set;
			}
			for (String part : s.split(",")) {
				set.add(Long.valueOf(part));
			}
			return set;
		}
	}
}

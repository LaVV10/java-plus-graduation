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

/**
 * Топология Kafka Streams сервиса Aggregator.
 *
 * <p>Поток обработки:
 * <pre>
 *   stats.user-actions.v1  ──►  source("user-actions-source")
 *                                    │
 *                                    ▼
 *                              processor("similarity-processor")
 *                                    │ читает/обновляет 4 state stores:
 *                                    │   user-action-store   "userId:eventId" → макс. вес
 *                                    │   event-weights-store eventId          → S_a (сумма весов)
 *                                    │   events-by-user-store userId          → Set&lt;eventId&gt;
 *                                    │   similarity-store     "a:b" (a&lt;b)      → S_min(a,b)
 *                                    ▼
 *                              sink("similarity-sink")  ──►  stats.events-similarity.v1
 * </pre>
 *
 * <p>State stores персистентные с changelog-топиками (префикс {@code aggregator-}),
 * создаваемыми Kafka Streams автоматически — состояние восстанавливается после рестарта.
 */
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

	/**
	 * Донастраивает {@link org.springframework.kafka.config.StreamsBuilderFactoryBean}:
	 * application.id (groupId Streams-приложения) и exactly-once.
	 */
	@Bean
	public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer() {
		return factory -> {
			Properties props = factory.getStreamsConfiguration();
			if (props == null) {
				props = new Properties();
			}
			props.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregator");
			props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
			factory.setStreamsConfiguration(props);
		};
	}

	/**
	 * Описывает topology: source-поток {@code stats.user-actions.v1} → процессор
	 * (4 state stores) → {@code stats.events-similarity.v1}.
	 *
	 * <p>Реализуется через DSL {@link StreamsBuilder}, который Spring Kafka Streams
	 * (через {@code @EnableKafkaStreams} + {@code defaultKafkaStreamsBuilder}) наполняет
	 * и потом сам вызывает {@code builder.build()}.
	 *
	 * <p>Avro-сериализация — Confluent wire format без Schema Registry через
	 * {@link AvroSerdes#forClass(Class)}: этого формата ждёт tester Практикума.
	 *
	 * @param builder бин {@link StreamsBuilder}, предоставляемый Spring Kafka auto-config
	 */
	@Bean
	public KStream<String, EventSimilarityAvro> aggregatorTopology(StreamsBuilder builder) {
		Serde<Set<Integer>> setSerde = Serdes.serdeFrom(new JsonSetSerializer(), new JsonSetDeserializer());
		Serde<UserActionAvro> inputSerde = AvroSerdes.forClass(UserActionAvro.class);
		Serde<EventSimilarityAvro> outputSerde = AvroSerdes.forClass(EventSimilarityAvro.class);

		// ─── State stores (key-value, persistent, с changelog-топиком) ──
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(USER_ACTION_STORE), Serdes.String(), Serdes.Double()));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(EVENT_WEIGHTS_STORE), Serdes.Integer(), Serdes.Double()));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(EVENTS_BY_USER_STORE), Serdes.Integer(), setSerde));
		builder.addStateStore(Stores.keyValueStoreBuilder(
				Stores.persistentKeyValueStore(SIMILARITY_STORE), Serdes.String(), Serdes.Double()));

		// ─── Граф: stream → process → to ────────────────────────────────
		KStream<String, UserActionAvro> source = builder.stream(
				USER_ACTIONS_TOPIC, Consumed.with(Serdes.String(), inputSerde));

		KStream<String, EventSimilarityAvro> similarities = source.process(
				() -> new SimilarityProcessor(USER_ACTION_STORE, EVENT_WEIGHTS_STORE,
						EVENTS_BY_USER_STORE, SIMILARITY_STORE),
				USER_ACTION_STORE, EVENT_WEIGHTS_STORE, EVENTS_BY_USER_STORE, SIMILARITY_STORE);

		similarities.to(EVENTS_SIMILARITY_TOPIC,
				org.apache.kafka.streams.kstream.Produced.with(Serdes.String(), outputSerde));

		return similarities;
	}

	/**
	 * Процессор инкрементального обновления сходства мероприятий.
	 *
	 * <p>Использует Processor API v3 ({@code org.apache.kafka.streams.processor.api.Processor}):
	 * метод {@code process} принимает {@link org.apache.kafka.streams.processor.api.Record},
	 * а вывод идёт через {@code context.forward(record)} — это позволяет получить выходной
	 * поток через {@code KStream.process(...)}, возвращающий {@code KStream<KOut,VOut>}.
	 *
	 * <p>При каждом новом действии пользователя (userId, eventId, action, ts):
	 * <ol>
	 *   <li>Берёт вес действия (VIEW=0.4 / REGISTER=0.8 / LIKE=1.0).</li>
	 *   <li>Если он не превышает текущий максимальный вес пользователя по этому мероприятию — игнор.</li>
	 *   <li>Иначе обновляет S_a (сумма весов по мероприятию) и S_min(A,B) для всех B,
	 *       с которыми пользователь уже взаимодействовал, и пересылает обновлённое сходство дальше.</li>
	 * </ol>
	 */
	public static final class SimilarityProcessor
			implements org.apache.kafka.streams.processor.api.Processor<String, UserActionAvro, String, EventSimilarityAvro> {
		private final String userActionStoreName;
		private final String eventWeightsStoreName;
		private final String eventsByUserStoreName;
		private final String similarityStoreName;

		private org.apache.kafka.streams.processor.api.ProcessorContext<String, EventSimilarityAvro> context;
		private KeyValueStore<String, Double> userActionStore;
		private KeyValueStore<Integer, Double> eventWeightsStore;
		private KeyValueStore<Integer, Set<Integer>> eventsByUserStore;
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
			this.eventWeightsStore = (KeyValueStore<Integer, Double>) context.getStateStore(eventWeightsStoreName);
			this.eventsByUserStore = (KeyValueStore<Integer, Set<Integer>>) context.getStateStore(eventsByUserStoreName);
			this.similarityStore = (KeyValueStore<String, Double>) context.getStateStore(similarityStoreName);
		}

		@Override
		public void process(org.apache.kafka.streams.processor.api.Record<String, UserActionAvro> record) {
			UserActionAvro action = record.value();
			if (action == null) {
				return;
			}
			int userId = action.getUserId();
			int eventId = action.getEventId();
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
			Set<Integer> userEvents = eventsByUserStore.get(userId);
			if (userEvents == null) {
				userEvents = new HashSet<>();
			}

			Instant timestamp = action.getTimestamp() == null ? Instant.now() : action.getTimestamp();

			// Пересчёт сходства со всеми остальными мероприятиями пользователя.
			for (Integer otherBoxed : userEvents) {
				int other = otherBoxed;
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

		private static String pairKey(int a, int b) {
			return a < b ? a + ":" + b : b + ":" + a;
		}

		private static EventSimilarityAvro buildSimilarity(int eventA, int eventB,
														   double score, Instant timestamp) {
			int a = Math.min(eventA, eventB);
			int b = Math.max(eventA, eventB);
			return EventSimilarityAvro.newBuilder()
					.setEventA(a)
					.setEventB(b)
					.setScore(score)
					.setTimestamp(timestamp)
					.build();
		}
	}

	/**
	 * Сериализатор {@code Set<Integer>} как comma-separated значений (для state store).
	 */
	static final class JsonSetSerializer implements org.apache.kafka.common.serialization.Serializer<Set<Integer>> {
		@Override
		public byte[] serialize(String topic, Set<Integer> data) {
			if (data == null) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Integer e : data) {
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
	 * Десериализатор {@code Set<Integer>} из comma-separated значений.
	 */
	static final class JsonSetDeserializer implements org.apache.kafka.common.serialization.Deserializer<Set<Integer>> {
		@Override
		public Set<Integer> deserialize(String topic, byte[] data) {
			if (data == null) {
				return new HashSet<>();
			}
			String s = new String(data, StandardCharsets.UTF_8);
			Set<Integer> set = new HashSet<>();
			if (s.isEmpty()) {
				return set;
			}
			for (String part : s.split(",")) {
				set.add(Integer.valueOf(part));
			}
			return set;
		}
	}
}

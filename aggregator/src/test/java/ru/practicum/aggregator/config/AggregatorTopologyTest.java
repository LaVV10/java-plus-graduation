package ru.practicum.aggregator.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

/**
 * Smoke-тест топологии Aggregator: стартует контекст Spring с embedded Kafka и
 * убеждается, что {@code defaultKafkaStreamsBuilder} поднимается без {@code TopologyException}.
 *
 * <p>Сам факт загрузки ApplicationContext при {@code @SpringBootTest} + {@code @EnableKafkaStreams}
 * и валиден, только если topology валидна: иначе Spring упадёт на фазе старта bean
 * {@code defaultKafkaStreamsBuilder} с {@code TopologyException}.
 *
 * <p>Ловит регрессии вида:
 * <ul>
 *   <li>«Topic ... has already been registered by another source» — двойная регистрация;</li>
 *   <li>«Topology has no stream threads» — пустой builder / игнорируемый бин Topology.</li>
 * </ul>
 */
@SpringBootTest(properties = {
		"spring.kafka.streams.properties.state.dir=/tmp/kafka-streams-aggregator-test",
		"spring.kafka.streams.properties.application.id=aggregator-test"
})
@EmbeddedKafka(partitions = 1, topics = {
		"stats.user-actions.v1", "stats.events-similarity.v1"
})
class AggregatorTopologyTest {

	@Test
	void applicationContextStartsWithValidTopology() {
		// Если topology невалидна — тест упадёт раньше, на загрузке ApplicationContext
		// (см. cause в логе surefire-reports/*AggregatorTopologyTest.txt).
	}
}

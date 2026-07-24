package ru.practicum.aggregator.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

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
	}
}

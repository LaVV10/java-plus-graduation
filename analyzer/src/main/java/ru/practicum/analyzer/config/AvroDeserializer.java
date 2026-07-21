package ru.practicum.analyzer.config;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Kafka-десериализатор Avro в Confluent Wire Format без Schema Registry.
 *
 * <p>Пропускает заголовок Confluent (magic byte 0 + 4 байта schema-id) и читает
 * чистый Avro-payload по известному классу схемы (передаётся в конструкторе).
 *
 * <p>Совместим с {@link ru.practicum.collector.config.AvroSerializer} на стороне Collector'а
 * и с тестером Практикума, который использует {@code KafkaAvroDeserializer}.
 */
public class AvroDeserializer<T extends SpecificRecord> implements Deserializer<T> {

	private static final byte MAGIC_BYTE = 0x00;
	private static final int HEADER_SIZE = 1 + Integer.BYTES;

	private final Class<T> clazz;

	public AvroDeserializer(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(String topic, byte[] data) {
		if (data == null) {
			return null;
		}
		try {
			T prototype = (T) clazz.getDeclaredConstructor().newInstance();
			SpecificDatumReader<T> reader = new SpecificDatumReader<>(prototype.getSchema());
			// Пропускаем заголовок Confluent, если он присутствует.
			int offset = hasConfluentHeader(data) ? HEADER_SIZE : 0;
			BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, offset, data.length - offset, null);
			return reader.read(null, decoder);
		} catch (Exception e) {
			throw new SerializationException("Avro-десериализация для топика " + topic, e);
		}
	}

	private static boolean hasConfluentHeader(byte[] data) {
		return data.length > HEADER_SIZE && data[0] == MAGIC_BYTE;
	}
}

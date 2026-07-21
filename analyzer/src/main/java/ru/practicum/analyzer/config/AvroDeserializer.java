package ru.practicum.analyzer.config;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Kafka-десериализатор Avro — чистый Avro binary, без Confluent wire format.
 *
 * <p>Тестер Практикума использует {@code ru.practicum.kafka.deserializer.BaseAvroDeserializer},
 * который читает payload с первого байта. Мы пишем и читаем тем же форматом.
 *
 * <p>Совместим с {@link ru.practicum.collector.config.AvroSerializer} на стороне Collector'а
 * и {@code GeneralAvroSerializer} тестера.
 */
public class AvroDeserializer<T extends SpecificRecord> implements Deserializer<T> {

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
			BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
			return reader.read(null, decoder);
		} catch (Exception e) {
			throw new SerializationException("Avro-десериализация для топика " + topic, e);
		}
	}
}

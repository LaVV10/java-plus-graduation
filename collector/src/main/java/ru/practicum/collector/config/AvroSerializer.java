package ru.practicum.collector.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Kafka-сериализатор для Avro-записей — чистый Avro binary, без Confluent wire format.
 *
 * <p>Тестер Практикума использует {@code ru.practicum.kafka.deserializer.BaseAvroDeserializer},
 * который читает payload с первого байта через {@code DecoderFactory.binaryDecoder(data, null)} —
 * без magic byte и schema-id. Поэтому пишем «голый» Avro-payload: {@link SpecificRecord} →
 * {@link BinaryEncoder}.
 *
 * <p>Это совпадает с {@code ru.practicum.kafka.serializer.GeneralAvroSerializer} из
 * {@code avro-schemas.jar} тестера.
 */
public class AvroSerializer<T extends SpecificRecord> implements Serializer<T> {

	@Override
	public byte[] serialize(String topic, T data) {
		if (data == null) {
			return null;
		}
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
			@SuppressWarnings("unchecked")
			SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
			writer.write(data, encoder);
			encoder.flush();
			return out.toByteArray();
		} catch (IOException e) {
			throw new SerializationException("Avro-сериализация для топика " + topic, e);
		}
	}
}

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
 * Kafka-сериализатор для Avro-записей в чистый binary-формат.
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

package ru.practicum.aggregator.config;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Утилита Serde для Avro-записей {@link SpecificRecord} — чистый Avro binary, без Confluent
 * wire format.
 *
 * <p>Тестер Практикума использует {@code ru.practicum.kafka.deserializer.BaseAvroDeserializer},
 * который читает payload с первого байта через {@code DecoderFactory.binaryDecoder(data, null)} —
 * без magic byte и schema-id. Поэтому сериализуем «голый» Avro-payload.
 *
 * <p>Совпадает с {@code ru.practicum.kafka.serializer.GeneralAvroSerializer} /
 * {@code BaseAvroDeserializer} из {@code avro-schemas.jar} тестера.
 */
public final class AvroSerdes {

	private AvroSerdes() {
	}

	/**
	 * Создаёт Serde для конкретного Avro-класса.
	 */
	public static <T extends SpecificRecord> Serde<T> forClass(Class<T> clazz) {
		return Serdes.serdeFrom(new AvroSerdeSerializer<>(), new AvroSerdeDeserializer<>(clazz));
	}

	/**
	 * Сериализатор: SpecificRecord → чистый Avro-payload.
	 */
	public static class AvroSerdeSerializer<T extends SpecificRecord> implements Serializer<T> {
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

	/**
	 * Десериализатор: чистый Avro-payload → SpecificRecord.
	 */
	public static class AvroSerdeDeserializer<T extends SpecificRecord> implements Deserializer<T> {
		private final Class<T> clazz;

		public AvroSerdeDeserializer(Class<T> clazz) {
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
}

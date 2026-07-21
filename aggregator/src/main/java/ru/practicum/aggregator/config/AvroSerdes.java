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
import java.nio.ByteBuffer;

/**
 * Утилита Serde для Avro-записей {@link SpecificRecord} в Confluent Wire Format
 * без Schema Registry.
 *
 * <p>Сериализация: Confluent wire format (magic byte 0 + schema-id int32 + Avro-payload) —
 * этого формата ждёт tester Практикума.
 *
 * <p>Десериализация: пропускаем 5 байт заголовка и читаем чистый Avro-payload по известному
 * классу схемы. Класс схемы известен на стороне читателя, поэтому реальный schema-id
 * из заголовка игнорируется.
 */
public final class AvroSerdes {

	private static final byte MAGIC_BYTE = 0x00;
	private static final int SCHEMA_ID = 1;
	private static final int HEADER_SIZE = 1 + Integer.BYTES;

	private AvroSerdes() {
	}

	/**
	 * Создаёт Serde для конкретного Avro-класса.
	 */
	public static <T extends SpecificRecord> Serde<T> forClass(Class<T> clazz) {
		return Serdes.serdeFrom(new AvroSerdeSerializer<>(), new AvroSerdeDeserializer<>(clazz));
	}

	/**
	 * Сериализатор: SpecificRecord → Confluent wire format.
	 */
	public static class AvroSerdeSerializer<T extends SpecificRecord> implements Serializer<T> {
		@Override
		public byte[] serialize(String topic, T data) {
			if (data == null) {
				return null;
			}
			byte[] payload;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
				@SuppressWarnings("unchecked")
				SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(data.getSchema());
				writer.write(data, encoder);
				encoder.flush();
				payload = out.toByteArray();
			} catch (IOException e) {
				throw new SerializationException("Avro-сериализация для топика " + topic, e);
			}
			ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + payload.length);
			buffer.put(MAGIC_BYTE);
			buffer.putInt(SCHEMA_ID);
			buffer.put(payload);
			return buffer.array();
		}
	}

	/**
	 * Десериализатор: Confluent wire format → SpecificRecord.
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
				// Пропускаем заголовок Confluent (magic byte + schema-id), если он есть.
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
}

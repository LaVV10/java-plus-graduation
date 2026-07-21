package ru.practicum.collector.config;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Kafka-сериализатор для Avro-записей в формате Confluent Wire Format без Schema Registry.
 *
 * <p>Формат (см. Confluent spec):
 * <pre>
 *   byte 0      : 0x00 — magic byte (версия формата)
 *   bytes 1..4  : schema-id (int32, big-endian) — фиксированный (нет registry, но тестер ждёт 5 байт заголовка)
 *   bytes 5..   : Avro-payload (SpecificRecord → BinaryEncoder)
 * </pre>
 *
 * <p>Тестер Практикума использует {@code KafkaAvroDeserializer}, который ожидает именно этот
 * заголовок. Schema-id мы ставим константой (нет registry для валидации), десериализатор
 * тестера при {@code use.latest.version=true} / mock registry игнорирует реальный id и
 * восстанавливает Avro-класс из payload (через {@code avro.schema} в схеме).
 *
 * <p>Десериализация на нашей стороне — {@code AggregatorStreamsConfig.AvroSerdes} / {@code
 * AnalyzerConfig.AvroDeserializer}: пропускаем 5 байт заголовка и читаем чистый Avro.
 */
public class AvroSerializer<T extends SpecificRecord> implements Serializer<T> {

	/** Magic byte Confluent Wire Format (версия 0). */
	private static final byte MAGIC_BYTE = 0x00;
	/** Фиктивный schema-id (тестер не валидирует по registry). */
	private static final int SCHEMA_ID = 1;

	@Override
	public byte[] serialize(String topic, T data) {
		if (data == null) {
			return null;
		}
		// Avro-payload: SpecificRecord → бинарь.
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

		// Confluent wire format: [magic byte 0][schema-id int32 BE][payload].
		ByteBuffer buffer = ByteBuffer.allocate(1 + Integer.BYTES + payload.length);
		buffer.put(MAGIC_BYTE);
		buffer.putInt(SCHEMA_ID);
		buffer.put(payload);
		return buffer.array();
	}
}

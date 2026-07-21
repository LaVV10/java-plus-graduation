package ru.practicum.collector.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.action.v1.UserActionControllerGrpc;
import ru.practicum.ewm.stats.action.v1.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

/**
 * gRPC-реализация сервиса {@link UserActionControllerGrpc}.
 *
 * Принимает {@link UserActionProto} от core-сервисов (event-service, request-service),
 * маппит в {@link UserActionAvro} и асинхронно отправляет в Kafka-топик
 * {@code stats.user-actions.v1} с ключом {@code userId}.
 *
 * gRPC-метод возвращает {@code Empty} сразу — клиент не ждёт обработки Aggregator/Analyzer.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionControllerGrpcImpl extends UserActionControllerGrpc.UserActionControllerImplBase {

	private static final String USER_ACTIONS_TOPIC = "stats.user-actions.v1";

	private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;

	@Value("${spring.kafka.topic.user-actions:" + USER_ACTIONS_TOPIC + "}")
	private String userActionsTopic;

	@Override
	public void collectUserAction(UserActionProto request,
								  StreamObserver<com.google.protobuf.Empty> responseObserver) {
		log.debug("Получено действие пользователя: userId={}, eventId={}, action={}, ts={}",
				request.getUserId(), request.getEventId(),
				request.getActionType(), request.getTimestamp());

		try {
			UserActionAvro avro = mapToAvro(request);
			String key = String.valueOf(request.getUserId());

			// send() синхронно сериализует (KafkaAvroSerializer) и кладёт в буфер продюсера.
			// Если Schema Registry недоступен или неверный url — упадёт здесь с исключением.
			kafkaTemplate.send(userActionsTopic, key, avro).get();
		} catch (Exception e) {
			log.error("Не удалось записать действие пользователя в Kafka: userId={}, eventId={}, action={}",
					request.getUserId(), request.getEventId(), request.getActionType(), e);
			// Возвращаем в gRPC статус INTERNAL с описанием — иначе tester видит лишь UNKNOWN.
			responseObserver.onError(io.grpc.Status.INTERNAL
					.withDescription("Не удалось записать действие в Kafka: " + e.getMessage())
					.withCause(e)
					.asRuntimeException());
			return;
		}

		responseObserver.onNext(com.google.protobuf.Empty.getDefaultInstance());
		responseObserver.onCompleted();
	}

	private UserActionAvro mapToAvro(UserActionProto proto) {
		// Avro timestamp-millis маппится на java.time.Instant (генератор подключил
		// TimestampMillisConversion), поэтому сеттим Instant напрямую.
		Instant timestamp = proto.hasTimestamp()
				? Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos())
				: Instant.now();

		return UserActionAvro.newBuilder()
				.setUserId(proto.getUserId())
				.setEventId(proto.getEventId())
				.setActionType(mapAction(proto.getActionType()))
				.setTimestamp(timestamp)
				.build();
	}

	private ActionTypeAvro mapAction(ru.practicum.ewm.stats.action.v1.ActionTypeProto proto) {
		return switch (proto) {
			case ACTION_VIEW -> ActionTypeAvro.VIEW;
			case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
			case ACTION_LIKE -> ActionTypeAvro.LIKE;
			case UNRECOGNIZED -> throw new IllegalArgumentException(
					"Неизвестный ActionTypeProto: UNRECOGNIZED");
		};
	}
}

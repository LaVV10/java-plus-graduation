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
 * gRPC-реализация сервиса сбора действий пользователей.
 *
 * Принимает {@link UserActionProto}, маппит в {@link UserActionAvro} и отправляет
 * в Kafka-топик {@code stats.user-actions.v1} с ключом userId.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionControllerGrpcImpl extends UserActionControllerGrpc.UserActionControllerImplBase {

	private static final String USER_ACTIONS_TOPIC = "stats.user-actions.v1";

	private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

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
			kafkaTemplate.send(userActionsTopic, request.getUserId(), avro).get();
		} catch (Exception e) {
			log.error("Не удалось записать действие пользователя в Kafka: userId={}, eventId={}, action={}",
					request.getUserId(), request.getEventId(), request.getActionType(), e);
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

package ru.practicum.stats.client;

import com.google.protobuf.Timestamp;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.action.v1.ActionTypeProto;
import ru.practicum.ewm.stats.action.v1.UserActionControllerGrpc;
import ru.practicum.ewm.stats.action.v1.UserActionProto;

import java.time.Instant;

/**
 * gRPC-клиент сервиса Collector.
 *
 * <p>Отправляет действия пользователей (просмотр/регистрация/лайк) в Collector, который
 * асинхронно кладёт их в Kafka-топик {@code stats.user-actions.v1}.
 *
 * <p>Адрес сервиса резолвится через Eureka: {@code discovery:///collector}
 * (см. {@code grpc.client.collector.address} в конфигах core-сервисов).
 *
 * <p>Вызовы fire-and-forget на стороне core-сервиса: ошибки не должны валить бизнес-операцию
 * (например, показ события). Поэтому {@link #collectUserActionSafe} проглатывает исключения.
 */
@Slf4j
@Service
public class CollectorClient {

	@GrpcClient("collector")
	private UserActionControllerGrpc.UserActionControllerBlockingStub stub;

	@PostConstruct
	void checkStub() {
		if (stub == null) {
			log.warn("Collector gRPC-стаб не инициализирован: проверьте grpc.client.collector.address");
		}
	}

	/**
	 * Отправить действие пользователя в Collector.
	 *
	 * @param userId    идентификатор пользователя
	 * @param eventId   идентификатор мероприятия
	 * @param action    тип действия (VIEW/REGISTER/LIKE)
	 * @param timestamp момент действия
	 */
	public void collectUserAction(long userId, long eventId, ActionTypeProto action, Instant timestamp) {
		UserActionProto request = UserActionProto.newBuilder()
				.setUserId(userId)
				.setEventId(eventId)
				.setActionType(action)
				.setTimestamp(toProtoTimestamp(timestamp))
				.build();
		stub.collectUserAction(request);
	}

	/**
	 * Отправить действие пользователя, не выбрасывая исключение при сбое gRPC.
	 * Удобен для fire-and-forget вызовов в бизнес-логике core-сервисов.
	 */
	public void collectUserActionSafe(long userId, long eventId, ActionTypeProto action, Instant timestamp) {
		try {
			collectUserAction(userId, eventId, action, timestamp);
		} catch (Exception e) {
			log.warn("Не удалось отправить действие {} пользователя {} по событию {} в Collector: {}",
					action, userId, eventId, e.getMessage());
		}
	}

	private static Timestamp toProtoTimestamp(Instant instant) {
		Instant ts = instant == null ? Instant.now() : instant;
		return Timestamp.newBuilder()
				.setSeconds(ts.getEpochSecond())
				.setNanos(ts.getNano())
				.build();
	}
}

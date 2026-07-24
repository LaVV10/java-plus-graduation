package ru.practicum.stats.client;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.recommendations.v1.HasInteractionRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.HasInteractionResponseProto;
import ru.practicum.ewm.stats.recommendations.v1.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendations.v1.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.recommendations.v1.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * gRPC-клиент сервиса Analyzer.
 *
 * <p>Запрашивает рекомендации мероприятий по трём алгоритмам
 * {@code RecommendationsController}: персональные рекомендации, похожие мероприятия,
 * сумма взаимодействий (используется для поля {@code rating} у события в event-service).
 *
 * <p>Адрес резолвится через Eureka: {@code discovery:///analyzer}
 * (см. {@code grpc.client.analyzer.address} в конфиге core-сервиса).
 */
@Slf4j
@Service
public class AnalyzerClient {

	@GrpcClient("analyzer")
	private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

	@PostConstruct
	void checkStub() {
		if (stub == null) {
			log.warn("Analyzer gRPC-стаб не инициализирован: проверьте grpc.client.analyzer.address");
		}
	}

	/**
	 * Персональные рекомендации мероприятий (предсказание оценки).
	 *
	 * @param userId     идентификатор пользователя
	 * @param maxResults ограничение количества
	 * @return поток {@link RecommendedEventProto}; пустой при сбое
	 */
	public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
		UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
				.setUserId(userId)
				.setMaxResults(maxResults)
				.build();
		Iterator<RecommendedEventProto> iterator = stub.getRecommendationsForUser(request);
		return asStream(iterator);
	}

	/**
	 * Похожие мероприятия (с исключением просмотренных данным пользователем).
	 *
	 * @param eventId    идентификатор мероприятия-образца
	 * @param userId     идентификатор пользователя
	 * @param maxResults ограничение количества
	 */
	public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
		SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
				.setEventId(eventId)
				.setUserId(userId)
				.setMaxResults(maxResults)
				.build();
		Iterator<RecommendedEventProto> iterator = stub.getSimilarEvents(request);
		return asStream(iterator);
	}

	/**
	 * Сумма максимальных весов взаимодействий по мероприятиям (для поля {@code rating}).
	 *
	 * @param eventIds список идентификаторов мероприятий
	 * @return список {@code [eventId, score]}; для отсутствующих — 0.0
	 */
	public List<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
		if (eventIds == null || eventIds.isEmpty()) {
			return List.of();
		}
		InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
				.addAllEventId(eventIds)
				.build();
		Iterator<RecommendedEventProto> iterator = stub.getInteractionsCount(request);
		return asStream(iterator).collect(Collectors.toList());
	}

	/**
	 * Безопасный вариант {@link #getInteractionsCount}: возвращает пустой список при сбое gRPC,
	 * чтобы показ событий не падал из-за недоступности Analyzer.
	 */
	public List<RecommendedEventProto> getInteractionsCountSafe(List<Long> eventIds) {
		try {
			return getInteractionsCount(eventIds);
		} catch (Exception e) {
			log.warn("Не удалось получить сумму взаимодействий из Analyzer для {}: {}", eventIds, e.getMessage());
			return List.of();
		}
	}

	/**
	 * Проверяет, взаимодействовал ли пользователь с мероприятием (валидация лайка).
	 * При сбое gRPC считает, что взаимодействия нет (false) — чтобы не разрешить лайк
	 * без предпросмотра.
	 */
	public boolean hasInteraction(long userId, long eventId) {
		try {
			HasInteractionRequestProto request = HasInteractionRequestProto.newBuilder()
					.setUserId(userId)
					.setEventId(eventId)
					.build();
			HasInteractionResponseProto response = stub.hasInteraction(request);
			return response.getHasInteraction();
		} catch (Exception e) {
			log.warn("Не удалось проверить взаимодействие user={} с event={} в Analyzer: {}",
					userId, eventId, e.getMessage());
			return false;
		}
	}

	private static Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
				false);
	}
}

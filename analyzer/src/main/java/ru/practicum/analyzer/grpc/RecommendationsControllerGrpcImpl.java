package ru.practicum.analyzer.grpc;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.dto.RecommendedEvent;
import ru.practicum.analyzer.service.RecommendationService;
import ru.practicum.ewm.stats.recommendations.v1.HasInteractionRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.HasInteractionResponseProto;
import ru.practicum.ewm.stats.recommendations.v1.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendations.v1.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.recommendations.v1.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendations.v1.UserPredictionsRequestProto;

import java.util.List;

/**
 * gRPC-реализация сервиса {@link RecommendationsControllerGrpc}.
 *
 * Делегирует вычисления {@link RecommendationService} и стримит результаты
 * {@link RecommendedEventProto}.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class RecommendationsControllerGrpcImpl
		extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

	private final RecommendationService recommendationService;

	@Override
	public void getRecommendationsForUser(UserPredictionsRequestProto request,
										  StreamObserver<RecommendedEventProto> responseObserver) {
		log.debug("getRecommendationsForUser: userId={}, maxResults={}",
				request.getUserId(), request.getMaxResults());
		List<RecommendedEvent> recommendations = recommendationService.getRecommendationsForUser(
				request.getUserId(), request.getMaxResults());
		emitAndComplete(recommendations, responseObserver);
	}

	@Override
	public void getSimilarEvents(SimilarEventsRequestProto request,
								 StreamObserver<RecommendedEventProto> responseObserver) {
		log.debug("getSimilarEvents: eventId={}, userId={}, maxResults={}",
				request.getEventId(), request.getUserId(), request.getMaxResults());
		List<RecommendedEvent> similar = recommendationService.getSimilarEvents(
				request.getEventId(), request.getUserId(), request.getMaxResults());
		emitAndComplete(similar, responseObserver);
	}

	@Override
	public void getInteractionsCount(InteractionsCountRequestProto request,
									 StreamObserver<RecommendedEventProto> responseObserver) {
		log.debug("getInteractionsCount: eventIdsCount={}", request.getEventIdCount());
		List<RecommendedEvent> counts = recommendationService.getInteractionsCount(
				request.getEventIdList());
		emitAndComplete(counts, responseObserver);
	}

	@Override
	public void hasInteraction(HasInteractionRequestProto request,
							   StreamObserver<HasInteractionResponseProto> responseObserver) {
		log.debug("hasInteraction: userId={}, eventId={}", request.getUserId(), request.getEventId());
		boolean has = recommendationService.hasInteraction(request.getUserId(), request.getEventId());
		responseObserver.onNext(HasInteractionResponseProto.newBuilder()
				.setHasInteraction(has)
				.build());
		responseObserver.onCompleted();
	}

	private static void emitAndComplete(List<RecommendedEvent> events,
										StreamObserver<RecommendedEventProto> responseObserver) {
		for (RecommendedEvent e : events) {
			responseObserver.onNext(RecommendedEventProto.newBuilder()
					.setEventId(e.eventId())
					.setScore((float) e.score())
					.build());
		}
		responseObserver.onCompleted();
	}
}

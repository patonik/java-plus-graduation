package ru.practicum.analyzer.grpc;


import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.analyzer.repository.ActionRepository;
import ru.practicum.analyzer.repository.SimilarityRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationsControllerImpl extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final ActionRepository actionRepository;
    private final SimilarityRepository similarityRepository;

    @Override
    public void getRecommendationsForUser(RecommendationsProto.UserPredictionsRequestProto request,
                                          StreamObserver<RecommendationsProto.RecommendedEventProto> responseObserver) {
        long userId = request.getUserId();
        long maxResults = request.getMaxResults();

        List<RecommendationsProto.RecommendedEventProto> recommendations = fetchUserRecommendations(userId, maxResults);

        recommendations.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilarEvents(RecommendationsProto.SimilarEventsRequestProto request,
                                 StreamObserver<RecommendationsProto.RecommendedEventProto> responseObserver) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        long maxResults = request.getMaxResults();

        List<RecommendationsProto.RecommendedEventProto> similarEvents =
            fetchSimilarEvents(eventId, userId, maxResults);

        similarEvents.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(RecommendationsProto.InteractionsCountRequestProto request,
                                     StreamObserver<RecommendationsProto.RecommendedEventProto> responseObserver) {
        List<Long> eventIds = request.getEventIdsList();

        List<RecommendationsProto.RecommendedEventProto> interactionCounts = fetchInteractionCounts(eventIds);

        interactionCounts.forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    private List<RecommendationsProto.RecommendedEventProto> fetchUserRecommendations(long userId, long maxResults) {
        List<Long> recentInteractedEvents = actionRepository.findRecentInteractionsByUser(userId, maxResults);

        Set<SimilarityRepository.SimilarEventResult> similarEventsExcludingUserInteractions = new HashSet<>(
            similarityRepository.findSimilarEventsExcludingUserInteractionsForMultipleEvents(recentInteractedEvents,
                userId));

        return similarEventsExcludingUserInteractions.stream()
            .sorted(Comparator.comparingDouble(SimilarityRepository.SimilarEventResult::getSimilarityScore).reversed())
            .limit(maxResults)
            .map(entry -> RecommendationsProto.RecommendedEventProto.newBuilder()
                .setEventId(entry.getSimilarEventId())
                .setScore(entry.getSimilarityScore())
                .build())
            .toList();
    }


    private List<RecommendationsProto.RecommendedEventProto> fetchSimilarEvents(long eventId, long userId,
                                                                                long maxResults) {
        List<SimilarityRepository.SimilarEventResult> similarEventResults =
            similarityRepository.findSimilarEventsExcludingUserInteractions(eventId, userId);

        return similarEventResults.stream()
            .limit(maxResults)
            .map(result -> RecommendationsProto.RecommendedEventProto.newBuilder()
                .setEventId(result.getSimilarEventId())
                .setScore(result.getSimilarityScore().floatValue())
                .build())
            .toList();
    }

    private List<RecommendationsProto.RecommendedEventProto> fetchInteractionCounts(List<Long> eventIds) {
        List<ActionRepository.InteractionCountResult> interactionCounts =
            actionRepository.findInteractionCountsByEventIds(eventIds);

        return interactionCounts.stream()
            .map(result -> RecommendationsProto.RecommendedEventProto.newBuilder()
                .setEventId(result.getEventId())
                .setScore(result.getTotalWeight().floatValue())
                .build())
            .toList();
    }
}


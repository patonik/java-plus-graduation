package ru.practicum.grpc;

import collector.ActionControllerGrpc;
import collector.UserAction.UserActionProto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.grpc.RecommendationsControllerGrpc;
import ru.practicum.analyzer.grpc.RecommendationsProto;

import java.util.Iterator;

@Component
public class EventServiceGrpcClients {

    @GrpcClient("collector")
    private ActionControllerGrpc.ActionControllerBlockingStub collectorClient;

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub analyzerClient;

    public void sendUserAction(UserActionProto userAction) {
        collectorClient.collectUserAction(userAction);
    }

    public Iterator<RecommendationsProto.RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
        RecommendationsProto.UserPredictionsRequestProto request =
            RecommendationsProto.UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();

        return analyzerClient.getRecommendationsForUser(request);
    }
}


package ru.practicum.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import collector.ActionControllerGrpc;
import collector.UserAction.UserActionProto;

@Component
public class RequestServiceGrpcClients {

    @GrpcClient("collector")
    private ActionControllerGrpc.ActionControllerBlockingStub collectorClient;

    public void sendUserRegistration(UserActionProto userAction) {
        collectorClient.collectUserAction(userAction);
    }
}


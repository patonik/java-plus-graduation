package ru.practicum.collector.controller;

import collector.ActionControllerGrpc;
import collector.UserAction;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.collector.handler.UserActionHandler;
import ru.practicum.collector.service.KafkaProducerService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@GrpcService
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActionController extends ActionControllerGrpc.ActionControllerImplBase {
    private final UserActionHandler userActionHandler;
    private final KafkaProducerService kafkaProducerService;

    public void collectUserAction(UserAction.UserActionProto userActionProto, StreamObserver<Empty> responseObserver) {
        try {
            UserActionAvro userActionAvro = userActionHandler.handle(userActionProto);
            kafkaProducerService.sendUserAction(userActionAvro);
            // Send success response back to the client
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            // Handle exceptions and send an error response
            responseObserver.onError(
                new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e)
                )
            );
        }
    }
}

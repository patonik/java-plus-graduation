package ru.practicum.collector.handler;

import collector.UserAction;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.Map;

@Component
public class UserActionHandler {
    private static final Map<UserAction.ActionTypeProto, Double> WEIGHTS =
        Map.of(UserAction.ActionTypeProto.ACTION_VIEW, 0.4, UserAction.ActionTypeProto.ACTION_REGISTER, 0.8,
            UserAction.ActionTypeProto.ACTION_LIKE, 1.0);

    public UserActionAvro handle(UserAction.UserActionProto userActionProto) {
        long userId = userActionProto.getUserId();
        long eventId = userActionProto.getEventId();
        double actionTypeAvro = WEIGHTS.get(userActionProto.getActionType());
        long timestamp = Timestamps.toMillis(userActionProto.getTimestamp());

        return UserActionAvro.newBuilder()
            .setUserId(userId)
            .setEventId(eventId)
            .setActionType(actionTypeAvro)
            .setTimestamp(Instant.ofEpochMilli(timestamp))
            .build();
    }
}

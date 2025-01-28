package ru.practicum.collector.handler;

import collector.UserAction;
import com.google.protobuf.util.Timestamps;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;

@Component
public class UserActionHandler {
    public UserActionAvro handle(UserAction.UserActionProto userActionProto) {
        long userId = userActionProto.getUserId();
        long eventId = userActionProto.getEventId();
        ActionTypeAvro actionTypeAvro = ActionTypeAvro.valueOf(userActionProto.getActionType().name());
        long timestamp = Timestamps.toMillis(userActionProto.getTimestamp());

        return UserActionAvro.newBuilder()
            .setUserId(userId)
            .setEventId(eventId)
            .setActionType(actionTypeAvro)
            .setTimestamp(Instant.ofEpochMilli(timestamp))
            .build();
    }
}

package ru.practicum.aggregator.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.data.EventStat;
import ru.practicum.aggregator.data.SimilarityStat;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AggregatorService {

    // Map to store user actions Map<UserID, Map<EventID, Weight>>
    private final Table<Long, Long, Double> userActionTable = HashBasedTable.create();
    private final Map<Long, EventStat> eventStatMap = new HashMap<>();
    // Map to store similarity stats Map<EventAID, Map<EventBID, SimilarityStat>>
    private final Table<Long, Long, SimilarityStat> eventSimilarityTable = HashBasedTable.create();
    private static final Map<ActionTypeAvro, Double> WEIGHT_MAP =
        Map.of(ActionTypeAvro.VIEW, 0.4, ActionTypeAvro.REGISTER, 0.8, ActionTypeAvro.LIKE, 1.0);
    private final Producer<String, EventSimilarityAvro> producer;

    public Optional<List<EventSimilarityAvro>> updateState(UserActionAvro userActionAvro) {

        Long userId = userActionAvro.getUserId();
        Long eventId = userActionAvro.getEventId();
        Double weight = WEIGHT_MAP.get(userActionAvro.getActionType());
        List<EventSimilarityAvro> eventSimilarityAvroList = new ArrayList<>();


        if (userActionTable.isEmpty()) {
            userActionTable.put(userId, eventId, weight);
            return Optional.empty();
        }
        EventStat eventStat = Optional.ofNullable(eventStatMap.get(eventId)).orElse(new EventStat());
        double current = Optional.ofNullable(userActionTable.get(userId, eventId)).orElse(0.0);
        if (weight > current) {
            double delta = weight - current;
            userActionTable.put(userId, eventId, weight);
            eventStat.setEventWeightSum(eventStat.getEventWeightSum() + delta);
            eventStat.updateEventWeightSumRoot();
            eventStatMap.put(eventId, eventStat);
            Map<Long, Double> row = userActionTable.row(userId);
            if (row.size() > 1) {
                for (Long l : row.keySet()) {
                    if (!l.equals(eventId)) {
                        if (weight < row.get(l)) {
                            long eventIdA = Math.min(eventId, l);
                            long eventIdB = Math.max(eventId, l);
                            SimilarityStat similarityStat =
                                Optional.ofNullable(eventSimilarityTable.get(eventIdA, eventIdB))
                                    .orElse(new SimilarityStat());
                            similarityStat.setEventMinSum(similarityStat.getEventMinSum() + delta);
                            similarityStat.updateSimilarity(eventStat.getEventWeightSumRoot(),
                                eventStatMap.get(l).getEventWeightSumRoot());
                            eventSimilarityAvroList.add(EventSimilarityAvro.newBuilder()
                                .setEventIdA(eventIdA)
                                .setEventIdB(eventIdB)
                                .setScore(similarityStat.getSimilarity())
                                .setTimestamp(Instant.now())
                                .build());
                            eventSimilarityTable.put(eventIdA, eventIdB,
                                similarityStat);
                        }
                    }
                }
            }
            if (!eventSimilarityTable.isEmpty()) {
                return Optional.of(eventSimilarityAvroList);
            }
        }
        return Optional.empty();
    }
}



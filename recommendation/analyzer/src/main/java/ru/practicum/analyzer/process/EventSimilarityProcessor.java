package ru.practicum.analyzer.process;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.entity.EventSimilarityMapper;
import ru.practicum.analyzer.repository.SimilarityRepository;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventSimilarityProcessor {

    private final SimilarityRepository similarityRepository;
    private final EventSimilarityMapper eventSimilarityMapper;

    @KafkaListener(topics = "${kafka.topics.event}", groupId = "analyzer-group")
    public void processEventSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        try {
            similarityRepository.save(eventSimilarityMapper.toEntity(eventSimilarityAvro));
            log.info("Processed EventSimilarityAvro for eventIdA: {}, eventIdB: {}",
                eventSimilarityAvro.getEventIdA(), eventSimilarityAvro.getEventIdB());
        } catch (Exception e) {
            log.error("Error processing EventSimilarityAvro: {}", eventSimilarityAvro, e);
        }
    }
}


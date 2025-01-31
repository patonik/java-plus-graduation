package ru.practicum.analyzer.process;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.entity.UserActionMapper;
import ru.practicum.analyzer.repository.ActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionProcessor {

    private final ActionRepository actionRepository;
    private final UserActionMapper userActionMapper;

    @KafkaListener(topics = "${kafka.topics.user}", groupId = "analyzer-group")
    public void processUserAction(UserActionAvro userActionAvro) {
        try {
            actionRepository.save(userActionMapper.toEntity(userActionAvro));
            log.info("Processed UserActionAvro for userId: {}, eventId: {}",
                userActionAvro.getUserId(), userActionAvro.getEventId());
        } catch (Exception e) {
            log.error("Error processing UserActionAvro: {}", userActionAvro, e);
        }
    }
}



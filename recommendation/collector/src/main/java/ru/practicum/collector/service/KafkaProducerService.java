package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, UserActionAvro> actionKafkaTemplate;
    @Value("${kafka.topics.user}")
    private String userActionTopic;

    public void sendUserAction(UserActionAvro userActionAvro) {
        try {
            actionKafkaTemplate.send(userActionTopic, userActionAvro);
        } catch (KafkaException e) {
            log.error("Failed to send user action to Kafka: {}", e.getMessage());
        }
    }
}

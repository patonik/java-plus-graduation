package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class KafkaProducerService {
    private final KafkaProducer<String, UserActionAvro> kafkaProducer;
    @Value("${kafka.topics.user}")
    private String userActionTopic;

    public void sendUserAction(UserActionAvro userActionAvro) {
        try {
            kafkaProducer.send(new ProducerRecord<>(userActionTopic, userActionAvro));
        } catch (Exception e) {
            log.error("Failed to send user action to Kafka: {}", e.getMessage());
        }
    }
}

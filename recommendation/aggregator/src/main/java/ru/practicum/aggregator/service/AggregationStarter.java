package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final Consumer<String, UserActionAvro> consumer;
    private final Producer<String, EventSimilarityAvro> producer;
    private final AggregatorService aggregatorService;
    @Value("${kafka.topics.user}")
    private String userActionTopic;
    @Value("${kafka.topics.event}")
    private String eventSimilarityTopic;

    private volatile boolean running = true;  // Flag to manage shutdown

    public void start() {
        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList(userActionTopic));

        try {
            while (running) {
                // Poll for events with a timeout of 1 second
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(Duration.ofSeconds(1));

                records.forEach(record -> {
                    try {
                        aggregatorService.updateState(record.value()).ifPresent(eventSimilarityAvros -> {
                            for (EventSimilarityAvro eventSimilarityAvro : eventSimilarityAvros) {
                                producer.send(
                                    new ProducerRecord<>(eventSimilarityTopic, eventSimilarityAvro)
                                );
                            }
                        });
                    } catch (Exception e) {
                        log.error("Error processing action: {}", record, e);
                    }
                });

                // Commit offsets after processing each batch of records
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            // Ignored to handle shutdown request
            log.error("Processing interrupted in Aggregator", e);
        } catch (Exception e) {
            log.error("Error during event processing in Aggregator", e);
        } finally {
            shutdown();
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Initiating shutdown of AggregationStarter");

        // Stop the polling loop
        running = false;

        try {
            // Flush and close producer to ensure all messages are sent
            producer.flush();
            producer.close();
            log.info("Producer closed successfully");

            // Close consumer after committing any outstanding offsets
            consumer.commitSync();
            consumer.close();
            log.info("Consumer closed successfully");
        } catch (Exception e) {
            log.error("Error while closing Kafka resources", e);
        }
    }
}


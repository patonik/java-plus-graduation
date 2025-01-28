package ru.practicum.collector.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.practicum.common.serializer.UserActionSerializer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, UserActionAvro> actionKafkaProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:29092");
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserActionSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "actionKafkaTemplate")
    public KafkaTemplate<String, UserActionAvro> actionKafkaTemplate() {
        return new KafkaTemplate<>(actionKafkaProducerFactory());
    }
}

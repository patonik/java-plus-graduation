package ru.practicum.aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.aggregator.service.AggregationStarter;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableDiscoveryClient
public class AggregatorApp {

    public static void main(String[] args) {
        // Start the Spring Boot application and get the configured application context
        ConfigurableApplicationContext context = SpringApplication.run(AggregatorApp.class, args);

        // Retrieve the AggregationStarter bean and start the main processing loop
        AggregationStarter aggregator = context.getBean(AggregationStarter.class);
        aggregator.start();
    }
}


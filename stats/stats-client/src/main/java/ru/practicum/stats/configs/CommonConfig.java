package ru.practicum.stats.configs;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.HttpStatsClient;
import ru.practicum.stats.HttpStatsClientImpl;
import ru.practicum.stats.HttpStatsClientLoggingDecorator;

@Configuration
public class CommonConfig {

    @Bean
    public RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public HttpStatsClient createHttpStatsServer(RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        var httpStatsServer = new HttpStatsClientImpl(restTemplate, discoveryClient);
        return new HttpStatsClientLoggingDecorator(httpStatsServer);
    }
}
package ru.practicum.interaction.client;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor publicEventInterceptor() {
        return new PublicEventInterceptor();
    }
}


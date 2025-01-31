package ru.practicum.analyzer.grpc;


import net.devh.boot.grpc.server.config.GrpcServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcServerConfig {

    @Bean
    public GrpcServerProperties grpcServerProperties() {
        return new GrpcServerProperties();
    }
}

package com.diobank.ledger.infrastructure.grpc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GrpcServerConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "ledger.grpc.server", name = "enabled", havingValue = "true", matchIfMissing = true)
    GrpcServerLifecycle grpcServerLifecycle(GrpcServerProperties properties) {
        return new GrpcServerLifecycle(properties);
    }
}


package com.diobank.ledger.infrastructure.grpc;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "ledger.grpc.server")
public record GrpcServerProperties(
        boolean enabled,
        @Min(1) @Max(65535) int port
) {
}


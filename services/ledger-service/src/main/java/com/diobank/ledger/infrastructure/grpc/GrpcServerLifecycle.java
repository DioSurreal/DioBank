package com.diobank.ledger.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.io.IOException;

class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final GrpcServerProperties properties;
    private final LedgerGrpcService ledgerGrpcService;
    private final GlobalExceptionInterceptor globalExceptionInterceptor;
    private Server server;

    GrpcServerLifecycle(GrpcServerProperties properties,
                        LedgerGrpcService ledgerGrpcService,
                        GlobalExceptionInterceptor globalExceptionInterceptor) {
        this.properties = properties;
        this.ledgerGrpcService = ledgerGrpcService;
        this.globalExceptionInterceptor = globalExceptionInterceptor;
    }

    @Override
    public void start() {
        try {
            server = ServerBuilder.forPort(properties.port())
                    .intercept(globalExceptionInterceptor)
                    .addService(ledgerGrpcService)
                    .build()
                    .start();
            log.info("gRPC Server started, listening on port {}", properties.port());
        } catch (IOException e) {
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("gRPC Server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return server != null && !server.isShutdown();
    }
}

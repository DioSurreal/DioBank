package com.diobank.ledger.infrastructure.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

class GrpcServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerLifecycle.class);

    private final GrpcServerProperties properties;
    private Server server;
    private boolean running;

    GrpcServerLifecycle(GrpcServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            server = ServerBuilder.forPort(properties.port()).build().start();
            running = true;
            log.info("gRPC server started on port {}", properties.port());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start gRPC server on port " + properties.port(), exception);
        }
    }

    @Override
    public void stop() {
        if (server == null) {
            running = false;
            return;
        }

        server.shutdown();
        try {
            if (!server.awaitTermination(30, TimeUnit.SECONDS)) {
                server.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}


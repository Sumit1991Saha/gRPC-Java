package com.github.saha.grpc.greet.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting gRPC Server");
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreetServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        // Blocks the main thread to not finish
        server.awaitTermination();
    }
}

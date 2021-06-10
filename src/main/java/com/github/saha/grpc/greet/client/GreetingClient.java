package com.github.saha.grpc.greet.client;

import com.proto.dummy.DummyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.net.ssl.SSLException;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        System.out.println("Hello I'm a gRPC client");

        GreetingClient main = new GreetingClient();
        main.run();
    }

    private void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //Sync client
        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);

        //Async Client
        DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }
}

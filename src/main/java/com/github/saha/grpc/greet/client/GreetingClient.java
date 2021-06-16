package com.github.saha.grpc.greet.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.net.ssl.SSLException;

public class GreetingClient {

    public static void main(String[] args) throws SSLException {
        GreetingClient greetingClient = new GreetingClient();
        greetingClient.run();
    }

    private void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //Sync client
        //DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        //Async Client
        //DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);
        //doUnary(syncClient);
        doServerStreaming(syncClient);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnary(GreetServiceGrpc.GreetServiceBlockingStub syncClient) {
        System.out.println("Starting gRPC Unary call");
        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("Sumit")
                                .setLastName("Saha")
                                .build())
                .build();
        GreetResponse response = syncClient.greet(request);
        System.out.println(response.getResult());
    }

    private void doServerStreaming(GreetServiceGrpc.GreetServiceBlockingStub syncClient) {
        System.out.println("Starting gRPC Server Streaming call");
        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("Sumit")
                                .setLastName("Saha")
                                .build())
                .build();
        syncClient.greetManyTimes(request)
                .forEachRemaining( greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }
}

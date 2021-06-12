package com.github.saha.grpc.greet.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.GreetServiceGrpc;
import com.proto.greet.Greeting;
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
        doUnary(syncClient);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnary(GreetServiceGrpc.GreetServiceBlockingStub syncClient) {
        System.out.println("Starting gRPC Unary call");
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Sumit")
                .setLastName("Saha")
                .build();
        GreetRequest request = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();
        GreetResponse response = syncClient.greet(request);

        System.out.println(response.getResult());
    }
}

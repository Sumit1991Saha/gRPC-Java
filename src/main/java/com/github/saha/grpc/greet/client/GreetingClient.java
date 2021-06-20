package com.github.saha.grpc.greet.client;

import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) throws SSLException, InterruptedException {
        System.out.println("Creating gRPC client");
        GreetingClient greetingClient = new GreetingClient();
        greetingClient.run();
    }

    private void run() throws SSLException, InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //doUnary(channel);
        //doServerStreaming(channel);
        doClientStreaming(channel);
        //doBiDiStreamingCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnary(ManagedChannel channel) {
        System.out.println("Starting gRPC Unary call");
        //Sync client
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

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

    private void doServerStreaming(ManagedChannel channel) {
        System.out.println("Starting gRPC Server Streaming call");
        //Sync client
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest request = GreetManyTimesRequest.newBuilder()
                .setGreeting(
                        Greeting.newBuilder()
                                .setFirstName("Sumit")
                                .setLastName("Saha")
                                .build())
                .build();
        syncClient.greetManyTimes(request)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }

    private void doClientStreaming(ManagedChannel channel) throws InterruptedException {
        System.out.println("Starting gRPC Server Streaming call");
        // create an asynchronous client
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<LongGreetRequest> requestObserver =
                asyncClient.longGreet(new StreamObserver<LongGreetResponse>() {
                    @Override
                    public void onNext(LongGreetResponse value) {
                        // we get a response from the server
                        System.out.println("Received a response from the server");
                        System.out.println(value.getResult());
                        // onNext will be called only once
                    }

                    @Override
                    public void onError(Throwable t) {
                        // we get an error from the server
                    }

                    @Override
                    public void onCompleted() {
                        // the server is done sending us data
                        // onCompleted will be called right after onNext()
                        System.out.println("Server has completed sending us something");
                        latch.countDown();
                    }
                });

        Greeting[] greetings = {
                Greeting.newBuilder()
                        .setFirstName("Sumit")
                        .setLastName("Saha")
                        .build(),
                Greeting.newBuilder()
                        .setFirstName("Sumit")
                        .setLastName("Saha2")
                        .build(),
                Greeting.newBuilder()
                        .setFirstName("Sumit")
                        .setLastName("Saha3")
                        .build()
        };

        Arrays.asList(greetings)
                .forEach(
                        greeting -> {
                            System.out.println("sending message :- " + greeting);
                            requestObserver.onNext(
                                    LongGreetRequest.newBuilder()
                                            .setGreeting(greeting)
                                            .build());
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });

        // we tell the server that the client is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver =
                asyncClient.greetEveryone(new StreamObserver<GreetEveryoneResponse>() {
                    @Override
                    public void onNext(GreetEveryoneResponse value) {
                        System.out.println("Response from server: " + value.getResult());
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Server is done sending data");
                        latch.countDown();
                    }
                });

        Arrays.asList("Stephane", "John", "Marc", "Patricia").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name))
                            .build());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

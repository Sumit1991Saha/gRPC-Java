package com.github.saha.grpc.greet.client;

import com.github.saha.grpc.greet.Constants;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
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
        ManagedChannel channel;
        if (Constants.useSSL) {
            channel = NettyChannelBuilder.forAddress("localhost", 50051)
                    .sslContext(
                            GrpcSslContexts.forClient()
                                    .trustManager(
                                            new File("ssl/ca.crt")
                                    )
                                    .build()
                    )
                    .build();
        } else {
            channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                    .usePlaintext()
                    .build();
        }

        doUnary(channel);
        //doServerStreaming(channel);
        //doClientStreaming(channel);
        //doBiDiStreamingCall(channel);
        //doSquareError(channel);
        //doUnaryWithDeadline(channel);

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

    private void doSquareError(ManagedChannel channel) {
        System.out.println("Starting gRPC Unary call for computing square root");
        //Sync client
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        computeSquareRoot(syncClient, -10);
        computeSquareRoot(syncClient, 100);
    }

    private void computeSquareRoot(GreetServiceGrpc.GreetServiceBlockingStub syncClient, int number) {
        try {
            SquareRootRequest request = SquareRootRequest.newBuilder()
                    .setNumber(number)
                    .build();
            SquareRootResponse response = syncClient.squareRoot(request);
            System.out.printf("Square root of %s is %s \n", number, response.getNumberRoot());
        } catch (StatusRuntimeException e) {
            System.out.println("Got an exception for square root");
            e.printStackTrace();
        }
    }

    private void doUnaryWithDeadline(ManagedChannel channel) {
        System.out.println("Starting gRPC Unary call with deadline");
        //Sync client
        GreetServiceGrpc.GreetServiceBlockingStub syncClient = GreetServiceGrpc.newBlockingStub(channel);

        // Server will respond after 300ms delay, so with 500ms as deadline,
        // deadline will not exceed but will exceed if the created client has deadline of 200ms ie < 300ms
        fetchWithDeadline(1000, syncClient);
        fetchWithDeadline(200, syncClient);
    }

    private void fetchWithDeadline(int deadlineLimitInMs, GreetServiceGrpc.GreetServiceBlockingStub syncClient) {
        try {
            System.out.printf("Sending a request with deadline of %s ms \n", deadlineLimitInMs);
            GreetServiceGrpc.GreetServiceBlockingStub clientWithinDeadline =
                    syncClient.withDeadlineAfter(deadlineLimitInMs, TimeUnit.MILLISECONDS);

            GreetWithDeadlineRequest request = GreetWithDeadlineRequest.newBuilder()
                    .setGreeting(
                            Greeting.newBuilder()
                                    .setFirstName("Sumit")
                                    .setLastName("Saha " + deadlineLimitInMs)
                                    .build())
                    .build();
            GreetWithDeadlineResponse response = clientWithinDeadline.greetWithDeadline(request);
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has been exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }

    }
}

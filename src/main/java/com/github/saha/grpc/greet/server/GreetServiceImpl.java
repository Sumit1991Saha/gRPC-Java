package com.github.saha.grpc.greet.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {

    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        System.out.println("Greet Unary request received");
        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // create the response
        String result = "Hello" + firstName + lastName;
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result)
                .build();

        // send the response
        responseObserver.onNext(response);

        // complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        /System.out.println("Greet Many times request received");
        // extract the fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();
        String lastName = greeting.getLastName();

        // create the response
        String result = "Hello" + firstName + lastName;
        GreetManyTimesResponse response;

        try {
            for (int i = 0; i < 10; ++i) {
                response = GreetManyTimesResponse.newBuilder()
                        .setResult(result + ", response no. " + i)
                        .build();
                // send the response
                responseObserver.onNext(response);
                System.out.println("Response sent :- " + response.toString());
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // complete the RPC call
            responseObserver.onCompleted();
        }
    }
}

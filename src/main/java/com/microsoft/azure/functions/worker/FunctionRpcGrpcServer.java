package com.microsoft.azure.functions.worker;

import com.microsoft.azure.functions.rpc.messages.FunctionRpcGrpc;
import com.microsoft.azure.functions.rpc.messages.StreamingMessage;
import com.microsoft.azure.functions.worker.handler.*;
import io.grpc.stub.StreamObserver;
import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;


public class FunctionRpcGrpcServer {

    private final int port;
    private final String workerId;
    private final Server server;

    public FunctionRpcGrpcServer(int port, String workerId) throws IOException {
            this(ServerBuilder.forPort(port), port, workerId);
    }

    public FunctionRpcGrpcServer(ServerBuilder<?> serverBuilder, int port, String workerId) {
            this.port = port;
            this.workerId = workerId;
            server = serverBuilder.addService(new FunctionRpcGrpcService(workerId)).build();
    }

    /** Start serving requests. */
    public void start() throws IOException {
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
    @Override
    public void run() {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            FunctionRpcGrpcServer.this.stop();
            System.err.println("*** server shut down");
            }});
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    private static class FunctionRpcGrpcService extends FunctionRpcGrpc.FunctionRpcImplBase {

        private String workerId;

        FunctionRpcGrpcService(String workerId) {
            this.workerId = workerId;
        }

        @Override
        public StreamObserver<StreamingMessage> eventStream(StreamObserver<StreamingMessage> responseObserver) {
            return new StreamObserver<StreamingMessage>() {
                @Override
                public void onNext(StreamingMessage message) {
                    MessageHandler<?, ?> handler = Something.handlerSuppliers.get(message.getContentCase()).get();
                    handler.setRequest(message);
                    handler.handle();
                    StreamingMessage.Builder messageBuilder = StreamingMessage.newBuilder();
                    handler.marshalResponse(messageBuilder);
                    responseObserver.onNext(messageBuilder.build());
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
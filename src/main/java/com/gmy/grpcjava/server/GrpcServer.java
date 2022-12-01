package com.gmy.grpcjava.server;

import gmy.grpc.examples.GreeterGrpc;
import gmy.grpc.examples.GreeterOuterClass;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @Author guomaoyang
 * @Date 2022/10/19
 */
public class GrpcServer {
    private Server server;
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 20880;
        server = ServerBuilder.forPort(port)
                .addService(new GreeterService())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    GrpcServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final GrpcServer server = new GrpcServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class GreeterService extends GreeterGrpc.GreeterImplBase{
        @Override
        public void sayHello(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
            logger.info("[普通模式]-接口到客户端信息："+request.getName());
            GreeterOuterClass.HelloReply reply = GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void sayHelloServerStream(GreeterOuterClass.HelloRequest request, StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
            logger.info("[服务端流模式]-接口到客户端信息："+request.getName());
            GreeterOuterClass.HelloReply reply = GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + request.getName()+1).build();
            responseObserver.onNext(reply);
            try {Thread.sleep(3000L);} catch (InterruptedException e) {e.printStackTrace();}

            GreeterOuterClass.HelloReply reply2 = GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + request.getName()+2).build();
            responseObserver.onNext(reply2);
            responseObserver.onCompleted();
        }

        @Override
        public StreamObserver<GreeterOuterClass.HelloRequest> sayHelloClientStream(StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
            return new StreamObserver<GreeterOuterClass.HelloRequest>() {
                private volatile boolean flag = true;
                @Override
                public void onNext(GreeterOuterClass.HelloRequest value) {
                    logger.info("[客户端流模式]接收到客户端请求：" + value.getName());

                    if(flag){
                        responseObserver.onNext(GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + value.getName()).build());
                        flag = false;
                    }
                }
                @Override
                public void onError(Throwable t) {

                }
                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    logger.info("流已关闭,响应结束...");
                }
            };
        }

        @Override
        public StreamObserver<GreeterOuterClass.HelloRequest> sayHelloBidirectionalStream(StreamObserver<GreeterOuterClass.HelloReply> responseObserver) {
            return new StreamObserver<GreeterOuterClass.HelloRequest>() {
                @Override
                public void onNext(GreeterOuterClass.HelloRequest value) {
                    logger.info("[双端流模式]接收到客户端请求：" + value.getName());
                    responseObserver.onNext(GreeterOuterClass.HelloReply.newBuilder().setMessage("Hello " + value.getName()).build());
                }
                @Override
                public void onError(Throwable t) {
                }
                @Override
                public void onCompleted() {
                    responseObserver.onCompleted();
                    logger.info("流已关闭,响应结束...");
                }
            };
        }
    }
}

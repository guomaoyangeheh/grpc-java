package com.gmy.grpcjava.client;

import com.gmy.grpcjava.server.GrpcServer;
import com.google.common.util.concurrent.ListenableFuture;
import gmy.grpc.examples.GreeterGrpc;
import gmy.grpc.examples.GreeterOuterClass;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author guomaoyang
 * @Date 2022/11/9
 */
public class GrpcClientUnary {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());


    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterFutureStub greeterFutureStub;
    private final GreeterGrpc.GreeterStub greeterStub;

    public GrpcClientUnary(Channel channel) {
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        greeterFutureStub = GreeterGrpc.newFutureStub(channel);
        greeterStub = GreeterGrpc.newStub(channel);
    }

    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:20880";

        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        try {
            GrpcClientUnary client = new GrpcClientUnary(channel);
            // 普通模式
            client.sayHelloForBlockingStub();

            Thread.sleep(10000L);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }


    private void sayHelloForBlockingStub(){
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName("world").build();
        // 实际也是调用了featureStub的方法实现
        GreeterOuterClass.HelloReply helloReply = blockingStub.sayHello(request);
        logger.info("接受到服务端响应:" + helloReply.getMessage());
    }

    private void sayHelloForNormalStub(){
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName("world").build();
        StreamObserveResponse streamObserveResponse = new StreamObserveResponse();
        try {
            greeterStub.sayHello(request,streamObserveResponse);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }

    private void sayHelloForFeatureStub(){
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName("world").build();
        ListenableFuture<GreeterOuterClass.HelloReply> helloReplyListenableFuture = greeterFutureStub.sayHello(request);
        while(helloReplyListenableFuture.isDone()){
            try {
                System.out.println("接受到服务端响应：" + helloReplyListenableFuture.get().getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class StreamObserveResponse implements StreamObserver<GreeterOuterClass.HelloReply>{


        @Override
        public void onNext(GreeterOuterClass.HelloReply value) {
            logger.info("接受到服务端响应:" + value.getMessage());
            // do something...

        }

        @Override
        public void onError(Throwable t) {

        }

        @Override
        public void onCompleted() {
            logger.info("服务端响应结束...");
        }
    }
}

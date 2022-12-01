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

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author guomaoyang
 * @Date 2022/11/9
 */
public class GrpcClientServerStream {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());


    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterFutureStub greeterFutureStub;
    private final GreeterGrpc.GreeterStub greeterStub;

    public GrpcClientServerStream(Channel channel) {
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
            GrpcClientServerStream client = new GrpcClientServerStream(channel);
            // 服务端流模式
            //client.serverStreamNormalStub();
            client.serverStreamBlockingStub();

            Thread.sleep(10000L);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void serverStreamNormalStub(){
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName("world").build();
        greeterStub.sayHelloServerStream(request,new StreamObserveResponse());
        // do something...
    }

    private void serverStreamBlockingStub(){
        GreeterOuterClass.HelloRequest request = GreeterOuterClass.HelloRequest.newBuilder().setName("world").build();
        Iterator<GreeterOuterClass.HelloReply> helloReplyIterator = blockingStub.sayHelloServerStream(request);
        while(helloReplyIterator.hasNext()){
            GreeterOuterClass.HelloReply next = helloReplyIterator.next();
            logger.info("接受到服务端响应:" + next.getMessage());
        }
        logger.info("服务端响应结束...");

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

package com.gmy.grpcjava.client;

import com.gmy.grpcjava.server.GrpcServer;
import gmy.grpc.examples.GreeterGrpc;
import gmy.grpc.examples.GreeterOuterClass;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @Author guomaoyang
 * @Date 2022/11/9
 */
public class GrpcClientBidirectionalStream {
    private static final Logger logger = Logger.getLogger(GrpcServer.class.getName());


    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final GreeterGrpc.GreeterFutureStub greeterFutureStub;
    private final GreeterGrpc.GreeterStub greeterStub;

    public GrpcClientBidirectionalStream(Channel channel) {
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
            GrpcClientBidirectionalStream client = new GrpcClientBidirectionalStream(channel);
            // 客户端流模式
            client.bidirectionalStream();

            Thread.sleep(10000L);
        } finally {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    private void bidirectionalStream(){
        // 编码与双端流一样
        StreamObserver<GreeterOuterClass.HelloRequest> requestStreamObserver = greeterStub.sayHelloBidirectionalStream(new StreamObserveResponse());
        for (int i = 0; i < 10; i++) {
            requestStreamObserver.onNext(GreeterOuterClass.HelloRequest.newBuilder().setName("world" + i).build());
        }
        //  请求结束
        requestStreamObserver.onCompleted();
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

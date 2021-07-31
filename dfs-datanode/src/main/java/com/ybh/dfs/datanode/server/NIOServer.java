package com.ybh.dfs.datanode.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;

import static com.ybh.dfs.datanode.server.DataNodeConfig.NIO_PORT;

/**
 * 数据节点nio server
 */
public class NIOServer extends Thread {
    public static final Integer PROCESSOR_THREAD_COUNT = 10;
    public static final Integer IO_THREAD_COUNT = 10;

    // NIO selector，负责多路复用监听多个连接的请求
    private Selector selector;

    private List<NIOProcessor> processors = new ArrayList<>();
    private NameNodeRpcClient namenode;

    /**
     * nioserver 初始化，监听端口，队列初始化，线程初始化
     */
    public NIOServer(NameNodeRpcClient nameNodeRpcClient){
        this.namenode = nameNodeRpcClient;
    }

    public void init() {
        ServerSocketChannel serverSocketChannel = null;

        try {

            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(NIO_PORT), 100);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("NIOServer已经启动，开始监听端口：" + NIO_PORT);
            NetworkResponseQueues networkResponse = NetworkResponseQueues.get();
            for(int i=0; i<PROCESSOR_THREAD_COUNT; i++) {
                NIOProcessor processor = new NIOProcessor(i);
                processors.add(processor);
                processor.start();
                networkResponse.initResponseQueue(i);
            }
            for(int i=0; i<IO_THREAD_COUNT; i++) {
                new IOThread(namenode).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        /**
         * 无限循环，等待IO 多路复用方式
         */
        while(true){
            try{
                selector.select();
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();

                while(keysIterator.hasNext()){
                    SelectionKey key = (SelectionKey) keysIterator.next();
                    keysIterator.remove();
                    if(key.isAcceptable()){
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        // 跟每个客户端建立连接
                        SocketChannel channel = serverSocketChannel.accept();
                        if(channel != null) {
                            channel.configureBlocking(false);
                            //建立连接之后，分发给后续processor线程
                            int index = new Random().nextInt(PROCESSOR_THREAD_COUNT);
                            NIOProcessor processor = processors.get(index);
                            processor.addChannel(channel);
                        }
                    }
                }
            }
            catch(Throwable t){
                t.printStackTrace();
            }
        }
    }

//

}
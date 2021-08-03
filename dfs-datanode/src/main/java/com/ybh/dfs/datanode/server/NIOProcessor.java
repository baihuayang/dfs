package com.ybh.dfs.datanode.server;

import java.io.IOException;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NIOProcessor extends Thread {
    private static final long MAX_SELECT_TIME = 1000;
    private ConcurrentLinkedQueue<SocketChannel> channelQueue = new ConcurrentLinkedQueue<>();  // todo 需要线程安全队列吗，或者使用阻塞队列呢？
    private Map<String, NetworkRequest> cachedRequest = new HashMap<>();
    private Map<String, NetworkResponse> cachedResponses = new HashMap<>();
    private Map<String, SelectionKey> cachedKeys = new HashMap<>();
    private Selector selector;
    private Integer processorId;

    public NIOProcessor(Integer processorId) {
        this.processorId = processorId;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Integer processorId) {
        this.processorId = processorId;
    }

    public void addChannel(SocketChannel channel) {
        channelQueue.offer(channel);
        selector.wakeup();
    }

    @Override
    public void run() {
        while(true) {
            try{
                registerQueueRegister();
                //处理队列中的响应
                cacheQueuedResponse();
                //限时阻塞方式
                poll();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cacheQueuedResponse() {
        NetworkResponseQueues networkResponseQueues = NetworkResponseQueues.get();
        NetworkResponse response = null;

        while((response = networkResponseQueues.poll(processorId)) != null) {
            String client = response.getClient();
            cachedResponses.put(client, response);
            SelectionKey selectionKey = cachedKeys.get(client);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void registerQueueRegister () {
        SocketChannel channel = null;
        while((channel = channelQueue.poll()) != null) { // todo  这里应该用阻塞的队列？
            try {
                channel.register(selector, SelectionKey.OP_READ);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            }
        }
    }

    public void poll() {
        try {
            int keys = selector.select(MAX_SELECT_TIME);
            if(keys > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while(iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    // 如果接受某个客户端请求
                    SocketChannel channel = (SocketChannel) key.channel();
                    String client = channel.getRemoteAddress().toString();
                    if(key.isReadable()) {
                        NetworkRequest request = null;
                        if(cachedRequest.containsKey(client)){
                            request = cachedRequest.get(client);
                        } else {
                            request = new NetworkRequest();
                            request.setChannel(channel);
                            request.setKey(key);
                        }

                        request.read();

                        if (request.hasCompletedRead()) {
                            // 此时就可以将一个请求分发到请求队列里去了
                            request.setProcessorId(processorId);
                            request.setClient(client);

                            NetworkRequestQueue networkRequestQueue = NetworkRequestQueue.get();
                            networkRequestQueue.offer(request);

                            cachedKeys.put(client, key);
                            cachedRequest.remove(client);

                            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                            //必须得等到一个客户端的一个请求处理完毕之后，才会允许读取下一个请求
                        } else {
                            cachedRequest.put(client, request);
                        }
                    } else if(key.isWritable()) {
                        NetworkResponse response = cachedResponses.get(client);
                        channel.write(response.getByteBuffer());

                        cachedResponses.remove(client);
                        cachedKeys.remove(client);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

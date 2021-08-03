package com.ybh.dfs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 网络连接池 // todo 这种长连接 会不会导致连接数过多 导致资源耗尽
 */
public class NetworkManager {
    // 正在连接中
    private static final Integer CONNECTING = 1;
    // 已经建立连接
    private static final Integer CONNECTED = 2;
    // 断开连接
    private static final Integer DISCONNECTED = 3;

    // 响应状态：成功
    private static final Integer RESPONSE_SUCCESS = 1;
    // 响应状态：失败
    private static final Integer RESPONSE_FAILURE = 2;
    // poll 时间间隔
    private static final Long POLL_TIMEOUT = 500L;
    //超时检测时间
    private static final Long REQUEST_TIMEOUT_CHECK_INTERVAL = 1000L;
    //超时时间
    private static final Long REQUEST_TIMEOUT = 3000L;
    // 多路复用器
    private Selector selector;
    //连接池
    private ConcurrentHashMap<String, SelectionKey> connections; //todo hostname 作为key 不可以啊，全是 localhost
    // 数据节点连接的装填
    private ConcurrentHashMap<String, Integer> connectState;
    // 等待建立连接的机器
    private ConcurrentLinkedQueue<Host> waitingConnectHosts;
    // 排队等待发送网络请求
    private Map<String, ConcurrentLinkedQueue<NetworkRequest>> waitingRequests;
    // 马上准备发送的请求
    private Map<String, NetworkRequest> toSendRequests;  // todo 这个数据结构意义是啥？？？
    // 已经完成请求的响应
    private Map<String, NetworkResponse> finishedResponse;
    // 还没读取完毕响应
    private Map<String, NetworkResponse> unfinishedResponse;


    public NetworkManager () {
        try{
            selector = Selector.open();
        }catch (Exception e){
            e.printStackTrace();
        }
        this.connections = new ConcurrentHashMap<String, SelectionKey>();
        this.connectState = new ConcurrentHashMap<String, Integer>();
        this.waitingConnectHosts = new ConcurrentLinkedQueue<>();
        this.waitingRequests = new ConcurrentHashMap<>();
        this.toSendRequests = new ConcurrentHashMap<>();
        this.finishedResponse = new ConcurrentHashMap<>();
        this.unfinishedResponse = new ConcurrentHashMap<>();

        new NetworkPollThread().start();
        new TimeoutCheckThread().start();
    }

    public Boolean maybeConnect(String hostname, Integer nioPort){
        synchronized (this) {  // todo 这里为什么要加锁
            if(!connectState.containsKey(hostname)
                    || connectState.get(hostname).equals(DISCONNECTED)){
                connectState.put(hostname, CONNECTING);
                waitingConnectHosts.offer(new Host(hostname, nioPort));
            }
            while(connectState.get(hostname).equals(CONNECTING)){
                try {
                    wait(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(connectState.get(hostname).equals(DISCONNECTED)) {
                return false;
            }
            return true;
        }
    }

    public void sendRequest(NetworkRequest request) {
        ConcurrentLinkedQueue<NetworkRequest> requestQueue = waitingRequests.get(request.getHostname());
        requestQueue.offer(request);
    }

    public NetworkResponse waitingResponse(String requestId) throws Exception{
        NetworkResponse response = null;
        while((response = finishedResponse.get(requestId)) == null) {
            Thread.sleep(100);
        }

        toSendRequests.remove(response.getHostname());
        finishedResponse.remove(requestId);

        return response;
    }

    /**
     * 网络连接核心线程
     */
    class NetworkPollThread extends Thread { // 在哪里启动？
        @Override
        public void run() {
            while(true) {
                tryConnect();
                prepareRequest();
                poll();
            }
        }

        private void tryConnect() {
            Host host = null;
            SocketChannel channel = null;
            while((host = waitingConnectHosts.poll()) != null) {
                try{
                    // 建立一个短连接
                    channel = SocketChannel.open();
                    channel.configureBlocking(false);
                    channel.connect(new InetSocketAddress(host.getHostname(), host.getNioPort()));
                    channel.register(selector, SelectionKey.OP_CONNECT);
                }catch (Exception e) {
                    e.printStackTrace();
                    connectState.put(host.getHostname(), DISCONNECTED);
                }
            }
        }

        private void prepareRequest() {
            for(String hostname : waitingRequests.keySet()) {
                ConcurrentLinkedQueue<NetworkRequest> requestQueue = waitingRequests.get(hostname);
                if(!requestQueue.isEmpty() && !toSendRequests.containsKey(hostname)) {
                    NetworkRequest request = requestQueue.poll();
                    toSendRequests.put(hostname, request);

                    SelectionKey key = connections.get(hostname);
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }

        private void poll() {
            SocketChannel channel = null;
            try{
                int selectedKeys = selector.select(POLL_TIMEOUT);
                if(selectedKeys <= 0){
                    return;
                }
                Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
                while(keysIterator.hasNext()){
                    SelectionKey key = keysIterator.next();
                    keysIterator.remove();
                    channel = (SocketChannel) key.channel();
                    if(key.isConnectable()) {
                        finishedConnect(key, channel);
                    } else if(key.isWritable()) {
                        sendRequest(key, channel);
                    } else if(key.isReadable()) {
                        readResponse(key, channel);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(channel != null){
                    try {
                        channel.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }

        private void finishedConnect(SelectionKey key, SocketChannel channel) throws Exception {
            InetSocketAddress remoteAddress = null;
            try{
                remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
                if (channel.isConnectionPending()) {
                    while (!channel.finishConnect()) {
                        Thread.sleep(100);
                    }
                    // 三次握手做完，tcp连接建立好
                }
                System.out.println("完成与服务端【" + remoteAddress.getHostName() + "】建立连接......");

                waitingRequests.put(remoteAddress.getHostName(), new ConcurrentLinkedQueue<NetworkRequest>());
                connections.put(remoteAddress.getHostName(), key);
                connectState.put(remoteAddress.getHostName(), CONNECTED);  //todo hostname 不够
            }catch (Exception e){
                e.printStackTrace();
                if(remoteAddress != null) {
                    connectState.put(remoteAddress.getHostName(), DISCONNECTED);
                }
            }
        }

        private void sendRequest(SelectionKey key, SocketChannel channel) {
            InetSocketAddress remoteAddress = null;
            try{
                remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
                String hostName = remoteAddress.getHostName();
                NetworkRequest request = toSendRequests.get(hostName);
                ByteBuffer buffer = request.getBuffer();
                channel.write(buffer);
                while(buffer.hasRemaining()){
                    channel.write(buffer);
                }

                System.out.println("本次向【"+hostName+"】请求发送完毕......");

                request.setSendTime(System.currentTimeMillis());

                key.interestOps(SelectionKey.OP_READ);
            }catch (Exception e) {
                e.printStackTrace();
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
                if( remoteAddress!= null){
                    String hostname = remoteAddress.getHostName();
                    NetworkRequest request = toSendRequests.get(hostname);
                    NetworkResponse response = new NetworkResponse();
                    response.setHostname(hostname);
                    response.setRequestId(request.getId());
                    response.setIp(request.getIp());
                    response.setError(true);
                    response.setFinished(true);

                    if(request.getNeedResponse()){
                        finishedResponse.put(request.getId(), response);
                    }else {
                        if(request.getCallback() != null) {
                            request.getCallback().process(response);
                        }
                        toSendRequests.remove(hostname);//todo 应该放上面if 前面
                    }
                }
            }

        }

        private void readResponse(SelectionKey key, SocketChannel channel) throws Exception {
            InetSocketAddress remoteAddress = (InetSocketAddress) channel.getRemoteAddress();
            String hostName = remoteAddress.getHostName();

            NetworkRequest request = toSendRequests.get(hostName);
            NetworkResponse response = null;

            if(request.getRequestType().equals(NetworkRequest.REQUEST_SEND_FILE)) {
               response = getSendFileResponse(request.getId(), hostName, channel );
            } else if(request.getRequestType().equals(NetworkRequest.REQUEST_READ_FILE)) {
                response = getReadFileResponse(request.getId(), hostName, channel );
            }
            if(!response.getFinished()) {
                return;
            }

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

            if(request.getNeedResponse()) {
                finishedResponse.put(request.getId(), response);
            } else {
                if(request.getCallback() != null) {
                    request.getCallback().process(response);
                }
                toSendRequests.remove(hostName);
            }
        }



        private NetworkResponse getSendFileResponse(String requestId, String hostname, SocketChannel channel) throws Exception {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            buffer.flip();

            NetworkResponse response = new NetworkResponse();
            response.setRequestId(requestId);
            response.setBuffer(buffer);
            response.setHostname(hostname);
            response.setError(false);
            response.setFinished(true);
            return response;
        }

        /**
         * 读取下载文件的响应
         * @param hostName
         * @param channel
         * @return
         */
        private NetworkResponse getReadFileResponse(String requestId, String hostName, SocketChannel channel)
                throws Exception{
            NetworkResponse response = null;

            if (!unfinishedResponse.containsKey(hostName)) {
                response = new NetworkResponse();
                response.setHostname(hostName);
                response.setRequestId(requestId);
                response.setError(false);
                response.setFinished(false);
            } else {
               response = unfinishedResponse.get(hostName);
            }

            Long fileLength = null;

            if(response.getBuffer() == null){
                ByteBuffer lengthBuffer = null;
                if(response.getLengthBuffer() == null){
                    lengthBuffer = ByteBuffer.allocate(NetworkRequest.FILE_LENGTH);
                    response.setLengthBuffer(lengthBuffer);
                }else {
                    lengthBuffer = response.getLengthBuffer();
                }

                channel.read(lengthBuffer);

                if(!lengthBuffer.hasRemaining()) {
                    lengthBuffer.rewind();
                    fileLength = lengthBuffer.getLong();
                } else {
                    unfinishedResponse.put(hostName, response);
                }
            }

            if(fileLength != null || response.getBuffer() != null){
                ByteBuffer buffer = null;

                if(response.getBuffer() == null) {
                    buffer = ByteBuffer.allocate(fileLength.intValue());
                    response.setBuffer(buffer);
                }else{
                    buffer = response.getBuffer();
                }

                channel.read(buffer);

                if(!buffer.hasRemaining()) {
                    buffer.rewind();
                    response.setFinished(true);
                    unfinishedResponse.remove(hostName);
                } else {
                    unfinishedResponse.put(hostName, response);
                }
            }

            return response;
        }

    }

    class TimeoutCheckThread extends Thread{
        @Override
        public void run() {
            while(true) {
                long now = System.currentTimeMillis();
                try{
                    for(NetworkRequest request : toSendRequests.values()) {
                        if(now - request.getSendTime() > REQUEST_TIMEOUT) {
                            String hostname = request.getHostname();
                            NetworkResponse response = new NetworkResponse();
                            response.setHostname(hostname);
                            response.setIp(request.getIp());
                            response.setRequestId(request.getId());
                            response.setError(true);
                            response.setFinished(true);

                            if(request.getNeedResponse()){
                                finishedResponse.put(request.getId(), response);
                            }else {
                                if(request.getCallback() != null) {
                                    request.getCallback().process(response);
                                }
                                toSendRequests.remove(hostname);
                            }
                        }
                    }

                    Thread.sleep(REQUEST_TIMEOUT_CHECK_INTERVAL);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

}

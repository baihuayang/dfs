package com.ybh.dfs.datanode.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;
import static com.ybh.dfs.datanode.server.DataNodeConfig.NIO_PORT;

/**
 * 数据节点nio server
 */
public class DataNodeNIOServer extends Thread {
    public static final Integer SEND_FILE = 1;
    public static final Integer READ_FILE = 2;
    public static final Integer NIO_BUFFER_SIZE = 10 * 1024;

    // NIO selector，负责多路复用监听多个连接的请求
    private Selector selector;
    // 内存队列
    private List<LinkedBlockingQueue<SelectionKey>> queues =
            new ArrayList<LinkedBlockingQueue<SelectionKey>>();
    // 缓存的没读取完的数据
    private Map<String, CachedRequests> cachedRequests = new ConcurrentHashMap<>();
    // 缓存没有读取完的requestType数据
    private Map<String, ByteBuffer> requestTypeByClient = new ConcurrentHashMap<>();
    // 缓存没有读取完的文件名长度数据
    private Map<String, ByteBuffer> filenameLengthByClient = new ConcurrentHashMap<>();
    // 缓存没有读取完的文件名数据
    private Map<String, ByteBuffer> filenameByClient = new ConcurrentHashMap<>();
    // 缓存没有读取完的文件e数据
    private Map<String, ByteBuffer> fileLengthByClient = new ConcurrentHashMap<>();
    // 缓存没有读取完的文件e数据
    private Map<String, ByteBuffer> fileByClient = new ConcurrentHashMap<>();
    // 与namenode 进行 rpc 通信的客户端
    private NameNodeRpcClient nameNodeRpcClient;

    /**
     * nioserver 初始化，监听端口，队列初始化，线程初始化
     */
    public DataNodeNIOServer(NameNodeRpcClient nameNodeRpcClient){
        ServerSocketChannel serverSocketChannel = null;

        try {
            this.nameNodeRpcClient = nameNodeRpcClient;

            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(NIO_PORT), 100);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            for(int i = 0; i < 3; i++) {
                queues.add(new LinkedBlockingQueue<SelectionKey>());
            }

            for(int i = 0; i < 3; i++) {
                new Worker(queues.get(i)).start();
            }

            System.out.println("NIOServer已经启动，开始监听端口：" + NIO_PORT);
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
                    handleEvents(key);
                }
            }
            catch(Throwable t){
                t.printStackTrace();
            }
        }
    }

    /**
     * 处理请求分发
     * @param key
     * @throws IOException
     * @throws ClosedChannelException
     */
    private void handleEvents(SelectionKey key)
            throws IOException, ClosedChannelException {
        SocketChannel channel = null;

        try{
            if(key.isAcceptable()){
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                channel = serverSocketChannel.accept();
                if(channel != null) {
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_READ);
                }
            } else if(key.isReadable()){
                channel = (SocketChannel) key.channel();
                String client = channel.getRemoteAddress().toString();
                int queueIndex = client.hashCode() % queues.size();
                queues.get(queueIndex).put(key);
            }
        }
        catch(Throwable t){
            t.printStackTrace();
            if(channel != null){
                channel.close();
            }
        }
    }

    /**
     * 处理请求的工作线程
     */
    class Worker extends Thread {

        private LinkedBlockingQueue<SelectionKey> queue;

        public Worker(LinkedBlockingQueue<SelectionKey> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while(true) {
                SocketChannel channel = null;

                try {
                    SelectionKey key = queue.take();
                    channel = (SocketChannel) key.channel();
                    handleRequest(channel, key);
                } catch (Exception e) {
                    e.printStackTrace();
                    if(channel != null) {
                        try {
                            channel.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理客户端发送过来的请求
     * @param channel
     * @param key
     * @throws Exception
     */
    private void handleRequest(SocketChannel channel, SelectionKey key) throws Exception{
        String client = channel.getRemoteAddress().toString();
        System.out.println("接收到客户端的请求:" + client);

        if (cachedRequests.containsKey(client)) {
            System.out.println("上一次请求出现拆包问题,本次继续执行文件上传操作......");
            handleSendFileRequest(channel, key);
            return;
        }
        // 提取请求类型
        Integer requestType = getRequestType(channel);
        if(requestType == null) {
            return;
        }
        System.out.println("从请求中解析出来的请求类型：" + requestType);
        if(SEND_FILE.equals(requestType)) {
            handleSendFileRequest(channel, key);
        }else if(READ_FILE.equals(requestType)){
            handleReadFileRequest(channel, key);
        }

    }

    /**
     * 发送文件
     * @param channel
     * @param key
     * @throws Exception
     */
    private void handleSendFileRequest(SocketChannel channel, SelectionKey key) throws Exception{
        String client = channel.getRemoteAddress().toString();

        // 从请求中解析文件名
        Filename filename = getFileName(channel); // D:\\dfs-test\\....jpg
        System.out.println("从网络请求中解析出来文件名:" + filename);
        if(filename == null) {
            return;
        }
        // 从请求中解析文件大小
        Long fileLength = getFileLength(channel);
        System.out.println("从网络请求中解析出来文件大小:" + fileLength);
        if(fileLength == null) {
            return;
        }
        // 获取已经读取的文件大小
        long hasReadImageLength = getHasReadFileLength(channel);
        System.out.println("初始化已经读取的文件大小:" + hasReadImageLength);

        // 构建针对本地文件的输出流
        FileOutputStream imageOut = null;
        FileChannel imageChannel = null;
        try{
            imageOut = new FileOutputStream(filename.absoluteFilename);
            imageChannel = imageOut.getChannel();
            imageChannel.position(imageChannel.size());
            System.out.println("对本地磁盘文件定位到position=" + imageChannel.size());

            ByteBuffer fileBuffer = null;
            if(fileByClient.containsKey(client)) {
                fileBuffer = fileByClient.get(client);
            }else {
                fileBuffer = ByteBuffer.allocate(fileLength.intValue());
            }

            hasReadImageLength += channel.read(fileBuffer);

            if (!fileBuffer.hasRemaining()) {
                fileBuffer.rewind();
                int written = imageChannel.write(fileBuffer);
                fileByClient.remove(client);
                System.out.println("本次文件上传完毕, 将" + written + " bytes的数据写入磁盘文件......");

                ByteBuffer outBuffer = ByteBuffer.wrap("SUCCESS".getBytes());
                channel.write(outBuffer);
                cachedRequests.remove(client);
                System.out.println("文件读取完毕，返回响应给客户端:" + client);
                // 增量上报Master节点，自己接收到了一个文件副本
                // /image/product/iphone.jpg
                nameNodeRpcClient.informReplicaReceived(filename.relativeFilename);
                System.out.println("增量上报收到的文件副本给NameNode节点......");
                // 关闭读取连接
                key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            } else {
                fileByClient.put(client, fileBuffer);
                getCachedRequests(client).hasReadFileLength = hasReadImageLength;
                System.out.println("本次文件上传出现拆包问题，缓存起来，下次继续读取......");
                return;
            }
        } finally {
            imageChannel.close();
            imageOut.close();
        }
    }


    /**
     * 读取文件
     * @param channel
     * @throws Exception
     */
    private void handleReadFileRequest(SocketChannel channel, SelectionKey key) throws Exception{
        String client = channel.getRemoteAddress().toString();

        // 从请求中解析文件名
        Filename filename = getFileName(channel); // D:\\dfs-test\\....jpg
        if(filename == null) {
            return;
        }
        // 构建针对本地文件的输入流
        File file = new File(filename.absoluteFilename);
        Long length = file.length();

        FileInputStream imageIn = new FileInputStream(filename.absoluteFilename);
        FileChannel imageChannel = imageIn.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(8 + length.intValue());
        buffer.putLong(length);
        int hasReadImageLength = imageChannel.read(buffer);
        System.out.println("从本地磁盘文件读取了" + hasReadImageLength + "bytes的数据");

        buffer.rewind();
        int sent = channel.write(buffer);
        System.out.println("将" + sent + " bytes的数据发送给客户端");

        imageChannel.close();
        imageIn.close();

        // 判断一下，如果已经读取完毕，就返回一个成功给客户端
        if(hasReadImageLength == length) {
            System.out.println("文件发送完毕,给客户端" + client);
            cachedRequests.remove(client);
            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
        }
    }

    /**
     * 从网络请求中获取文件名
     * @param channel
     * @return
     * @throws Exception
     */
    private Filename getFileName(SocketChannel channel) throws Exception {
        Filename filename = new Filename();
        String client = channel.getRemoteAddress().toString();

        if(getCachedRequests(client).filename != null) {
            return cachedRequests.get(client).filename;
        } else {
            String relativeFilename = getRelativeFilename(channel);
            if(relativeFilename == null) {
                return null;
            }
            // /image/product/iphne.jpg
            filename.relativeFilename = relativeFilename;
            filename.absoluteFilename = getAbsoluteFilename(relativeFilename);
            CachedRequests cachedRequests = getCachedRequests(client);
            cachedRequests.filename = filename;
        }
        return filename;
    }

    /**
     * 从网络请求获取文件大小
     * @param channel
     * @return
     * @throws Exception
     */
    private Long getFileLength(SocketChannel channel) throws Exception {
        Long fileLength = null;
        String client = channel.getRemoteAddress().toString();

        if(getCachedRequests(client).fileLength != null) {
            return cachedRequests.get(client).fileLength;
        } else {
            ByteBuffer fileLengthBuffer = null;
            if(fileLengthByClient.containsKey(client)) {
                fileLengthBuffer = fileLengthByClient.get(client);
            } else {
                fileLengthBuffer = ByteBuffer.allocate(8);
            }
            channel.read(fileLengthBuffer);
            if(!fileLengthBuffer.hasRemaining()) {
                fileLengthBuffer.rewind();
                fileLength = fileLengthBuffer.getLong();
                fileLengthByClient.remove(client);
                getCachedRequests(client).fileLength = fileLength;
            } else {
                fileLengthByClient.put(client, fileLengthBuffer);
            }
        }
        return fileLength;
    }

    /**
     * 获取请求类型
     * @param channel
     * @return
     */
    private Integer getRequestType(SocketChannel channel) throws Exception{
        Integer requestType = null;
        String client = channel.getRemoteAddress().toString();

        if(getCachedRequests(client) != null){
            return getCachedRequests(client).requestType;
        }

        ByteBuffer requestTypeBuffer = null;
        if(requestTypeByClient.containsKey(client)){
            requestTypeBuffer = requestTypeByClient.get(client);
        } else {
            requestTypeBuffer  = ByteBuffer.allocate(4);
        }

        channel.read(requestTypeBuffer);

        if (!requestTypeBuffer.hasRemaining()){
            requestTypeBuffer.rewind();
            requestType = requestTypeBuffer.getInt();
            System.out.println("从请求中解析出请求的类型：" + requestType);
            requestTypeByClient.remove(client);
            CachedRequests cachedRequests = getCachedRequests(client);
            cachedRequests.requestType = requestType;
        } else {
            requestTypeByClient.put(client, requestTypeBuffer);
        }
        return requestType;
    }

    private CachedRequests getCachedRequests(String client) {
        CachedRequests cachedRequests = this.cachedRequests.get(client);
        if(cachedRequests == null) {
            this.cachedRequests.put(client, new CachedRequests());
        }
        return cachedRequests;
    }

    /**
     * 获取相对路径的文件名
     * @param channel
     * @return
     * @throws Exception
     */
    private String getRelativeFilename(SocketChannel channel) throws Exception{
        String client = channel.getRemoteAddress().toString();
        Integer filenameLength = null;
        String filename = null;

        if(!filenameByClient.containsKey(client)) {
            ByteBuffer filenameLengthBuffer = null;
            if(filenameLengthByClient.containsKey(client)) {
                filenameLengthBuffer = filenameLengthByClient.get(client);
            } else {
                filenameLengthBuffer = ByteBuffer.allocate(4);
            }

            channel.read(filenameLengthBuffer);

            if(!filenameLengthBuffer.hasRemaining()){
                filenameLengthBuffer.rewind();
                filenameLength = filenameLengthBuffer.getInt();
                filenameLengthByClient.remove(client);
            } else {
                filenameLengthByClient.put(client, filenameLengthBuffer);
                return null;
            }
        }

        ByteBuffer filenameBuffer = null;
        if(filenameByClient.containsKey(client)) {
            filenameBuffer = filenameByClient.get(client);
        } else {
            filenameBuffer = ByteBuffer.allocate(filenameLength);
        }

        channel.read(filenameBuffer);

        if(!filenameBuffer.hasRemaining()){
            filenameBuffer.rewind();
            filename = new String(filenameBuffer.array());
            filenameByClient.remove(client);
        } else {
            filenameByClient.put(client, filenameBuffer);
        }
        return filename;
    }

    private String getAbsoluteFilename(String relativeFilename) {
        String[] relativeFilenameSplited = relativeFilename.split("/");
        String dirPath = DATA_DIR;
        for(int i=0; i<relativeFilenameSplited.length-1; i++){
            if( i == 0){
                continue;
            }
            dirPath += "\\" +relativeFilenameSplited[i];
        }
        File dir = new File(dirPath);
        if(!dir.exists()){
            return null;
        }

        return dirPath + "\\" + relativeFilenameSplited[relativeFilenameSplited.length-1];
    }



    /**
     * 获取已经读取文件大小
     * @param channel
     * @return
     * @throws Exception
     */
    private Long getHasReadFileLength(SocketChannel channel) throws Exception {
        String client = channel.getRemoteAddress().toString();
        if(getCachedRequests(client).hasReadFileLength != null) {
            return getCachedRequests(client).hasReadFileLength;
        }
        return 0L;
    }

    /**
     * 文件名类
     */
    class Filename {
        String relativeFilename;
        String absoluteFilename;

        @Override
        public String toString() {
            return "Filename{" +
                    "filename='" + relativeFilename + '\'' +
                    ", absoluteFilename='" + absoluteFilename + '\'' +
                    '}';
        }
    }

    /**
     * 缓存好的文件
     */
    class CachedRequests {

        Integer requestType;
        Filename filename;
        Long fileLength;
        Long hasReadFileLength;
    }

}
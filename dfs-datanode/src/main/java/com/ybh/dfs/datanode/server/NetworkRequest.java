package com.ybh.dfs.datanode.server;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;

public class NetworkRequest {
    public static final Integer REQUEST_SEND_FILE = 1;
    public static final Integer REQUEST_READ_FILE = 2;
    private SocketChannel channel;
    private SelectionKey key;
    private Integer processorId;
    private String client;

    // 缓存的没读取完的数据
    CachedRequests cachedRequest = new CachedRequests();
    ByteBuffer cachedrequestTypeBuffer;
    ByteBuffer cachedfilenameLengthBuffer;
    ByteBuffer cachedfilenameBuffer;
    ByteBuffer cachedfileLengthBuffer;
    ByteBuffer cachedfileBuffer;

    public SocketChannel getChannel() {
        return channel;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SelectionKey getKey() {
        return key;
    }

    public void setKey(SelectionKey key) {
        this.key = key;
    }

    public void read() {
        try{

            Integer requestType = null;
            if(cachedRequest.requestType != null) {
                requestType = cachedRequest.requestType;
            } else {
                requestType = getRequestType(channel);
            }
            if(requestType == null) {
                return;
            }
            System.out.println("从请求中解析出来的请求类型：" + requestType);
            if(REQUEST_SEND_FILE.equals(requestType)) {
                handleSendFileRequest(channel, key);
            }else if(REQUEST_READ_FILE.equals(requestType)){
                handleReadFileRequest(channel, key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Boolean hasCompletedRead() {
        return cachedRequest.hasCompletedRead;
    }

    /**
     * 获取请求类型
     * @param channel
     * @return
     */
    private Integer getRequestType(SocketChannel channel) throws Exception{
        Integer requestType = null;

        if(cachedRequest.requestType != null){
            return cachedRequest.requestType;
        }

        ByteBuffer requestTypeBuffer = null;
        if(cachedrequestTypeBuffer != null){
            requestTypeBuffer = cachedrequestTypeBuffer;
        } else {
            requestTypeBuffer  = ByteBuffer.allocate(4);
        }

        channel.read(requestTypeBuffer);

        if (!requestTypeBuffer.hasRemaining()){
            requestTypeBuffer.rewind();
            requestType = requestTypeBuffer.getInt();
            cachedRequest.requestType = requestType;
        } else {
            cachedrequestTypeBuffer = requestTypeBuffer;
        }
        return requestType;
    }


    /**
     * 发送文件
     * @param channel
     * @param key
     * @throws Exception
     */
    private void handleSendFileRequest(SocketChannel channel, SelectionKey key) throws Exception{
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

        ByteBuffer fileBuffer = null;
        if(cachedfileBuffer != null) {
            fileBuffer = cachedfileBuffer;
        }else {
            fileBuffer = ByteBuffer.allocate(fileLength.intValue());
        }

        channel.read(fileBuffer);
        if (!fileBuffer.hasRemaining()) {
            fileBuffer.rewind();
            cachedRequest.file = fileBuffer;
            cachedRequest.hasCompletedRead = true;
            System.out.println("本次文件上传请求读取完毕");
        } else {
            cachedfileBuffer = fileBuffer;
            System.out.println("本次文件上传出现拆包问题，缓存起来，下次继续读取......");
            return;
        }
    }


    /**
     * 读取文件
     * @param channel
     * @throws Exception
     */
    private void handleReadFileRequest(SocketChannel channel, SelectionKey key) throws Exception{
        // 从请求中解析文件名
        Filename filename = getFileName(channel); // D:\\dfs-test\\....jpg
        if(filename == null) {
            return;
        }
        cachedRequest.hasCompletedRead = true;
//        // 构建针对本地文件的输入流
//        File file = new File(filename.absoluteFilename);
//        Long length = file.length();
//
//        FileInputStream imageIn = new FileInputStream(filename.absoluteFilename);
//        FileChannel imageChannel = imageIn.getChannel();
//
//        ByteBuffer buffer = ByteBuffer.allocate(8 + length.intValue());
//        buffer.putLong(length);
//        int hasReadImageLength = imageChannel.read(buffer);
//        System.out.println("从本地磁盘文件读取了" + hasReadImageLength + "bytes的数据");
//
//        buffer.rewind();
//        int sent = channel.write(buffer);
//        System.out.println("将" + sent + " bytes的数据发送给客户端");
//
//        imageChannel.close();
//        imageIn.close();
//
//        // 判断一下，如果已经读取完毕，就返回一个成功给客户端
//        if(hasReadImageLength == length) {
//            System.out.println("文件发送完毕,给客户端");
//            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
//        }
    }

    /**
     * 从网络请求中获取文件名
     * @param channel
     * @return
     * @throws Exception
     */
    private Filename getFileName(SocketChannel channel) throws Exception {
        Filename filename = new Filename();

        if(cachedRequest.filename != null) {
            return cachedRequest.filename;
        } else {
            String relativeFilename = getRelativeFilename(channel);
            if(relativeFilename == null) {
                return null;
            }
            String absoluteFilename = getAbsoluteFilename(relativeFilename);
            // /image/product/iphne.jpg
            filename.relativeFilename = relativeFilename;
            filename.absoluteFilename = absoluteFilename;
            cachedRequest.filename = filename;
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

        if(cachedRequest.fileLength != null) {
            return cachedRequest.fileLength;
        } else {
            ByteBuffer fileLengthBuffer = null;
            if(cachedfileLengthBuffer != null) {
                fileLengthBuffer = cachedfileLengthBuffer;
            } else {
                fileLengthBuffer = ByteBuffer.allocate(8);
            }

            channel.read(fileLengthBuffer);

            if(!fileLengthBuffer.hasRemaining()) {
                fileLengthBuffer.rewind();
                fileLength = fileLengthBuffer.getLong();
                cachedRequest.fileLength = fileLength;
            } else {
                cachedfileLengthBuffer = fileLengthBuffer;
            }
        }
        return fileLength;
    }


    /**
     * 获取相对路径的文件名
     * @param channel
     * @return
     * @throws Exception
     */
    private String getRelativeFilename(SocketChannel channel) throws Exception{
        Integer filenameLength = null;
        String filename = null;

        if(cachedRequest.filenameLength == null) {
            ByteBuffer filenameLengthBuffer = null;
            if(cachedfilenameLengthBuffer != null) {
                filenameLengthBuffer = cachedfilenameLengthBuffer;
            } else {
                filenameLengthBuffer = ByteBuffer.allocate(4);
            }

            channel.read(filenameLengthBuffer);

            if(!filenameLengthBuffer.hasRemaining()){
                filenameLengthBuffer.rewind();
                filenameLength = filenameLengthBuffer.getInt();
                cachedRequest.filenameLength = filenameLength;
            } else {
                cachedfilenameLengthBuffer = filenameLengthBuffer;
                return null;
            }
        }

        ByteBuffer filenameBuffer = null;
        if(cachedfilenameBuffer != null) {
            filenameBuffer = cachedfilenameBuffer;
        } else {
            filenameBuffer = ByteBuffer.allocate(filenameLength);
        }
        channel.read(filenameBuffer);

        if(!filenameBuffer.hasRemaining()){
            filenameBuffer.rewind();
            filename = new String(filenameBuffer.array());
        } else {
            cachedfilenameBuffer = filenameBuffer;
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
            dirPath += "\\" + relativeFilenameSplited[i];
        }
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        return dirPath + "\\" + relativeFilenameSplited[relativeFilenameSplited.length-1];
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
        Integer filenameLength;
        Long fileLength;
        ByteBuffer file;
        Boolean hasCompletedRead = false;
    }

    public Integer getRequestType() {
        return cachedRequest.requestType;
    }

    public String getAbsoluteFilename() {
        return cachedRequest.filename.absoluteFilename;
    }

    public String getRelativeFilename() {
        return cachedRequest.filename.relativeFilename;
    }

    public Long getFileLength() {
        return cachedRequest.fileLength;
    }

    public ByteBuffer getFile() {
        return cachedRequest.file;
    }

    public Integer getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Integer processorId) {
        this.processorId = processorId;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}

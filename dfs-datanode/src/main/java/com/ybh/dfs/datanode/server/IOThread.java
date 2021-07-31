package com.ybh.dfs.datanode.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IOThread extends Thread {
    public static final Integer REQUEST_SEND_FILE = 1;
    public static final Integer REQUEST_READ_FILE = 2;
    private NetworkRequestQueue requestQueue = NetworkRequestQueue.get();
    private NameNodeRpcClient namenode;

    public IOThread (NameNodeRpcClient namenode) {
        this.namenode = namenode;
    }

    @Override
    public void run() {
        while(true) {
            try{
                NetworkRequest request = requestQueue.poll();
                if(request == null) {
                    Thread.sleep(100);
                }
                Integer requestType = request.getRequestType();
                if(requestType.equals(REQUEST_SEND_FILE)) {
                    writeToLocalDisk(request);
                } else if(requestType.equals(REQUEST_READ_FILE)) {
                    readFileFromLocalDisk(request);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void writeToLocalDisk(NetworkRequest request) throws Exception{
        FileOutputStream localFileOut = null;
        FileChannel localFileChannel = null;
        try{
            localFileOut = new FileOutputStream(request.getAbsoluteFilename());
            localFileChannel = localFileOut.getChannel();
            localFileChannel.position(localFileChannel.size());
            System.out.println("对本地磁盘文件定位到position=" + localFileChannel.size());

            int written = localFileChannel.write(request.getFile());
            System.out.println("本次文件上传完毕，将" + written + " bytes的数据写入本地磁盘文件......");

            namenode.informReplicaReceived(request.getRelativeFilename() + "_" + request.getFileLength());
            System.out.println("增量上报收到的文件副本给NameNode节点......");
            //封装响应
            NetworkResponse response = new NetworkResponse();
            response.setClient(request.getClient());
            response.setByteBuffer(ByteBuffer.wrap("SUCCESS".getBytes()));

            NetworkResponseQueues networkResponseQueue = NetworkResponseQueues.get();
            networkResponseQueue.offer(request.getProcessorId(), response);
        } finally {
            localFileOut.close();
            localFileChannel.close();
        }
    }

    private void readFileFromLocalDisk(NetworkRequest request) throws Exception {
        FileInputStream localFileIn = null;
        FileChannel localFileChannel = null;
        try{
            File file = new File(request.getAbsoluteFilename());
            Long length = file.length();

            localFileIn = new FileInputStream(request.getAbsoluteFilename());
            localFileChannel = localFileIn.getChannel();

            ByteBuffer buffer = ByteBuffer.allocate(8 + length.intValue());
            buffer.putLong(length);
            int hasReadImageLength = localFileChannel.read(buffer);
            System.out.println("从本地磁盘文件读取了" + hasReadImageLength + "bytes的数据");
            buffer.rewind();

            //封装响应
            NetworkResponse response = new NetworkResponse();
            response.setClient(request.getClient());
            response.setByteBuffer(buffer);

            NetworkResponseQueues networkResponseQueue = NetworkResponseQueues.get();
            networkResponseQueue.offer(request.getProcessorId(), response);
        } finally {
            if(localFileChannel != null){
                localFileChannel.close();
            }
            if(localFileIn != null) {
                localFileIn.close();
            }
        }
    }
}

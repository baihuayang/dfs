package com.ybh.dfs.datanode.server;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATANODE_IP;
import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;

/**
 * 复制任务管理器
 */
public class ReplicaManager {
    public static final Integer THREAD_NUM = 3;
    private ConcurrentLinkedQueue<JSONObject> replicaQueue = new ConcurrentLinkedQueue<>();

    private NIOClient nioClient =  new NIOClient();
    private NameNodeRpcClient nameNodeRpcClient = new NameNodeRpcClient();

    public ReplicaManager() {
        for(int i=0; i<THREAD_NUM; i++) {
            new Task().start();
        }
    }

    public void addReplicaQueue(JSONObject replicateTask) {
        this.replicaQueue.add(replicateTask);
    }

    class Task extends Thread {
        @Override
        public void run() {
            FileOutputStream imageOut = null;
            FileChannel imageChannel = null;
            JSONObject replicateTask = null;
            while(true) {
                try{
                    replicateTask = replicaQueue.poll();
                    if(replicateTask == null) {
                        Thread.sleep(1000);
                        continue;
                    }

                    System.out.println("开始执行副本复制的任务......");

                    //解析任务
                    String filename = replicateTask.getString("filename");
                    Long fileLength = replicateTask.getLong("fileLength");

                    JSONObject sourceNodeNode = replicateTask.getJSONObject("sourceNodeInfo");
                    String hostname = sourceNodeNode.getString("hostname");
                    Integer nioPort = sourceNodeNode.getInteger("nioPort");

                    byte[] file = nioClient.readFile(hostname, nioPort, filename);
                    ByteBuffer fileBuffer = ByteBuffer.wrap(file);
                    System.out.println("从源头数据节点 读取到图片，大小为：" + file.length + " 字节");

                    String absoluteFilename = FileUtils.getAbsoluteFilename(filename);
                    imageOut = new FileOutputStream(absoluteFilename);
                    imageChannel = imageOut.getChannel();
                    imageChannel.write(fileBuffer);
                    System.out.println("将图片写入本地磁盘文件，路径为：" + absoluteFilename);

                    nameNodeRpcClient.informReplicaReceived(filename + "_" + fileLength);
                    System.out.println("向master节点进行增量上报......,上报节点为 ip=" + DATANODE_IP);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    try {
                        if(imageChannel != null) {
                            imageChannel.close();
                        }
                        if(imageOut != null) {
                            imageOut.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}

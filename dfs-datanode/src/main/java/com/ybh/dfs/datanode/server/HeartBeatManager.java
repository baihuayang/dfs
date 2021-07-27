package com.ybh.dfs.datanode.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ybh.dfs.namenode.rpc.model.HeartbeatRequest;
import com.ybh.dfs.namenode.rpc.model.HeartbeatResponse;

import java.io.File;

import static com.ybh.dfs.datanode.server.DataNodeConfig.*;

/**
 * 心跳管理器
 */
public class HeartBeatManager {

    public static final Integer SUCCESS = 1;
    public static final Integer FAILURE = 2;
    public static final Integer COMMAND_REGISTER = 1;
    public static final Integer COMMAND_REPORT_COMPLETE_DATANODES = 2;
    public static final Integer COMMAND_REPLICATE = 3;
    public static final Integer COMMAND_REMOVE_REPLICATE = 4;
    private NameNodeRpcClient nameNodeRpcClient;

    private StorageManager storageManager;

    private ReplicaManager replicaManager;

    public HeartBeatManager() {

    }


    public HeartBeatManager(NameNodeRpcClient nameNodeRpcClient, StorageManager storageManager, ReplicaManager replicaManager) {
        this.nameNodeRpcClient = nameNodeRpcClient;
        this.storageManager = storageManager;
        this.replicaManager = replicaManager;
    }

    public void start() {
        new HeartbeatThread().start();
    }

    public NameNodeRpcClient getNameNodeRpcClient() {
        return nameNodeRpcClient;
    }

    /**
     * 负责心跳的线程
     * @author zhonghuashishan
     *
     */
    class HeartbeatThread extends Thread {

        @Override
        public void run() {
                System.out.println("定时心跳线程启动......");
                while(true) {
                    try {
                        HeartbeatResponse response = nameNodeRpcClient.heartbeat();
                        if(response.getStatus() == SUCCESS) {
                            JSONArray commands = JSONArray.parseArray(response.getCommands());
                            if(commands.size() > 0) {
                                for(int i=0;i<commands.size();i++) {
                                    JSONObject jsonObject = commands.getJSONObject(i);
                                    Integer type = jsonObject.getInteger("type");
                                    JSONObject task = jsonObject.getJSONObject("content");
                                    if(type.equals(COMMAND_REPLICATE)) {
                                        replicaManager.addReplicaQueue(task);
                                        System.out.println("接收到副本复制的任务，" + commands);
                                    } else if (type.equals(COMMAND_REMOVE_REPLICATE)) {
                                        //删除副本
                                        String filename = task.getString("filename");
                                        String absoluteFilename = FileUtils.getAbsoluteFilename(filename);
                                        File file = new File(absoluteFilename);
                                        if(file.exists()){
                                            file.delete();
                                        }
                                    }
                                }
                            }
                        }
                        // 心跳失败了
                        if(response.getStatus() == FAILURE){
                            JSONArray commands = JSONArray.parseArray(response.getCommands());
                            for(int i=0;i<commands.size();i++){
                                JSONObject command = commands.getJSONObject(i);
                                Integer type = command.getInteger("type");
                                if(type.equals(COMMAND_REGISTER)){
                                    nameNodeRpcClient.register();
                                } else if(type.equals(COMMAND_REPORT_COMPLETE_DATANODES)) {
                                    StorageInfo storageInfo = storageManager.getStorageInfo();
                                    nameNodeRpcClient.reportCompleteStorageInfo(storageInfo);
                                }
                            }
                        }
                    }catch (Exception e) {
                        System.out.println("当前namenode不可用，心跳失败.....");
                    }
                    try {
                        Thread.sleep(30 * 1000); // 每隔30秒发送一次心跳到NameNode上去
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }

}

package com.ybh.dfs.datanode.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ybh.dfs.namenode.rpc.model.HeartbeatRequest;
import com.ybh.dfs.namenode.rpc.model.HeartbeatResponse;

import static com.ybh.dfs.datanode.server.DataNodeConfig.*;

/**
 * 心跳管理器
 */
public class HeartBeatManager {

    private NameNodeRpcClient nameNodeRpcClient;

    private StorageManager storageManager;

    public HeartBeatManager() {

    }


    public HeartBeatManager(NameNodeRpcClient nameNodeRpcClient, StorageManager storageManager) {
        this.nameNodeRpcClient = nameNodeRpcClient;
        this.storageManager = storageManager;
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

                        // 心跳失败了
                        if(response.getStatus() == 2){
                            JSONArray commands = JSONArray.parseArray(response.getCommands());
                            for(int i=0;i<commands.size();i++){
                                JSONObject command = commands.getJSONObject(i);
                                Integer type = command.getInteger("type");
                                if(type.equals(1)){
                                    nameNodeRpcClient.register();
                                } else if(type.equals(2)) {
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

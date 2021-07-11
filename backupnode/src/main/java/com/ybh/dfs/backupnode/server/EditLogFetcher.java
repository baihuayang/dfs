package com.ybh.dfs.backupnode.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;

/**
 * 从namenode同步editslog组件
 */
public class EditLogFetcher extends Thread{
    public static final Integer BACKUP_NODE_FETCH_SIZE = 10;

    private BackupNode backupNode;
    private NameNodeRpcClient nameNode;
    private FSNamesystem namesystem;

    public EditLogFetcher(BackupNode backupNode, FSNamesystem namesystem){
        this.backupNode = backupNode;
        this.nameNode = new NameNodeRpcClient();
        this.namesystem = namesystem;
    }

    @Override
    public void run() {
        System.out.println("Editslog 抓取线程已经启动......");

        while (backupNode.isRunning()){
            try{
                JSONArray editsLogs = nameNode.fetchEditsLog();
                if(editsLogs.size() == 0) {
                    System.out.println("没有拉取到任何一条editslog, 等待1秒后继续拉取");
                    Thread.sleep(1000);
                    continue;
                }

                if(editsLogs.size() < BACKUP_NODE_FETCH_SIZE) {
                    Thread.sleep(1000);
                    System.out.println("拉取到editslog不足10条数据, 等待1秒后继续拉取");
                }

                for(int i=0;i<editsLogs.size();i++){
                    JSONObject editsLog = editsLogs.getJSONObject(i);
                    System.out.println("拉取到一条editslog:" + editsLog.toJSONString());
                    String op = editsLog.getString("OP");
                    if(op.equals("MKDIR")){
                        String path = editsLog.getString("PATH");
                        try {
                            namesystem.mkdir(editsLog.getLongValue("txid"), path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

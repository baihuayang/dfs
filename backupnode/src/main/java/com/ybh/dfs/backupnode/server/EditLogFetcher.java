package com.ybh.dfs.backupnode.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 从namenode同步editslog组件
 */
public class EditLogFetcher extends Thread{
    public static final Integer BACKUP_NODE_FETCH_SIZE = 10;

    private BackupNode backupNode;
    private NameNodeRpcClient nameNode;
    private FSNamesystem namesystem;

    public EditLogFetcher(BackupNode backupNode, FSNamesystem namesystem, NameNodeRpcClient namenode){
        this.backupNode = backupNode;
        this.nameNode = namenode;
        this.namesystem = namesystem;
    }

    @Override
    public void run() {
        System.out.println("Editslog 抓取线程已经启动......");

        while (backupNode.isRunning()){
            try{
                if(!namesystem.isFinishedRecover()) {
                    System.out.println("当前还没完成元数据恢复，不进行editlog同步......");
                    Thread.sleep(1000);
                    continue;
                }

                long syncedTxid = namesystem.getSyncedTxid();
                JSONArray editsLogs = nameNode.fetchEditsLog(syncedTxid);

                if(editsLogs.size() == 0) {
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
                    } else if(op.equals("CREATE")) {
                        String filename = editsLog.getString("PATH");
                        try{
                            namesystem.create(editsLog.getLongValue("txid"), filename);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                nameNode.setNammenodeRunning(true);
            }catch (Exception e){
//                e.printStackTrace();
                nameNode.setNammenodeRunning(false);
            }
        }
    }
}

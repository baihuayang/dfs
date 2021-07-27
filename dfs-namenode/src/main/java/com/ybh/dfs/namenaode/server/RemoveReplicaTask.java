package com.ybh.dfs.namenaode.server;

/**
 * 删除副本任务
 */
public class RemoveReplicaTask {
    private String filename;
    private DataNodeInfo dataNode;

    public RemoveReplicaTask(String filename, DataNodeInfo dataNode) {
        this.filename = filename;
        this.dataNode = dataNode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DataNodeInfo getDataNode() {
        return dataNode;
    }

    public void setDataNode(DataNodeInfo dataNode) {
        this.dataNode = dataNode;
    }
}

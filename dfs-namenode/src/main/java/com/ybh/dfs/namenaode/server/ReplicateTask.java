package com.ybh.dfs.namenaode.server;

/**
 * 副本复制任务
 */
public class ReplicateTask {
    public ReplicateTask(String filename, Long fileLength, DataNodeInfo sourceNodeInfo, DataNodeInfo destNodeInfo) {
        this.filename = filename;
        this.fileLength = fileLength;
        this.sourceNodeInfo = sourceNodeInfo;
        this.destNodeInfo = destNodeInfo;
    }

    private String filename;
    private Long fileLength;
    private DataNodeInfo sourceNodeInfo;
    private DataNodeInfo destNodeInfo;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }

    public DataNodeInfo getSourceNodeInfo() {
        return sourceNodeInfo;
    }

    public void setSourceNodeInfo(DataNodeInfo sourceNodeInfo) {
        this.sourceNodeInfo = sourceNodeInfo;
    }

    public DataNodeInfo getDestNodeInfo() {
        return destNodeInfo;
    }

    public void setDestNodeInfo(DataNodeInfo destNodeInfo) {
        this.destNodeInfo = destNodeInfo;
    }

    @Override
    public String toString() {
        return "ReplicateTask{" +
                "filename='" + filename + '\'' +
                ", fileLength=" + fileLength +
                ", sourceNodeInfo=" + sourceNodeInfo +
                ", destNodeInfo=" + destNodeInfo +
                '}';
    }
}

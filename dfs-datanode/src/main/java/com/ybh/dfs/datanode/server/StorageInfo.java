package com.ybh.dfs.datanode.server;

import java.util.ArrayList;
import java.util.List;

/**
 * 存储信息
 */
public class StorageInfo {
    private List<String> filenames = new ArrayList<>();
    private Long storedDataSize = 0L;

    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public Long getStoredDataSize() {
        return storedDataSize;
    }

    public void setStoredDataSize(Long storedDataSize) {
        this.storedDataSize = storedDataSize;
    }

    public void addFilename(String filename) {
        this.filenames.add(filename);
    }

    public void addStoredDatasize(Long storedDataSize) {
        this.storedDataSize += storedDataSize;
    }

    @Override
    public String toString() {
        return "StorageInfo{" +
                "filenames=" + filenames +
                ", storedDataSize=" + storedDataSize +
                '}';
    }
}

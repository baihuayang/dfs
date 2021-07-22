package com.ybh.dfs.datanode.server;

import java.io.File;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;

/**
 * 磁盘管理组件
 */
public class StorageManager {
    public StorageInfo getStorageInfo() {
        StorageInfo storageInfo = new StorageInfo();

        File dir = new File(DATA_DIR);
        File[] children = dir.listFiles();
        if(children == null || children.length == 0) {
            return null;
        }

        for(File child : children) {
            scanFile(child, storageInfo);
        }
        return storageInfo;
    }

    private void scanFile(File dir, StorageInfo storageInfo) {
        if(dir.isFile()) {
            String path = dir.getPath();
            path = path.substring(DATA_DIR.length());
            System.out.println("扫描到一个文件 文件名为:" + path + " 大小为:" + dir.length());

            path = path.replace("\\", "/");
            storageInfo.addFilename(path);
            storageInfo.addStoredDatasize(dir.length());
            return;
        }

        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            return;
        }
        for(int i=0; i<files.length; i++) {
            scanFile(files[i], storageInfo);
        }
    }
}

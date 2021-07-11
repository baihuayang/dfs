package com.ybh.dfs.backupnode.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * fsimage文件的checkpoint组件
 */
public class FsImageCheckpointer extends Thread{
    /**
     * checkpoint 操作的时间间隔
     */
    public static final Integer CHECKPOINT_INTERVAL = 2 * 60 * 1000;

    private BackupNode backupNode;
    private FSNamesystem namesystem;
    private String lastFImageFile = "";

    public FsImageCheckpointer(BackupNode backupNode, FSNamesystem namesystem) {
        this.backupNode = backupNode;
        this.namesystem = namesystem;
    }

    @Override
    public void run() {
        System.out.println("fsimage checkpoint 定时调度线程启动......");
        while(backupNode.isRunning()){
            try{
                Thread.sleep(CHECKPOINT_INTERVAL);
                // 触发checkpoint操作
                // 写数据的过程中，你必须是
                System.out.println("准备执行checkpoint操作，写入fsimage文件。。。。。。");
                FSImage fsImage = namesystem.getFSImageJson();
                removeLastFsimageFile();
                doCheckpoint(fsImage);
            }catch (Exception e){

            }
        }
    }

    /**
     * 将fsimage 持久化到磁盘上
     * @param fsImage
     * @throws Exception
     */
    private void doCheckpoint(FSImage fsImage) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(fsImage.getFsimageJson().getBytes());

        // fsimage 文件名的格式，包含当前最后一个edits.log txid
        String fsimageFile = "D:\\dfs-test\\backupnode\\fsimage-" + (fsImage.getMaxTxid()) + ".meta";
        lastFImageFile = fsimageFile;

        RandomAccessFile file = null;
        FileOutputStream out = null;
        FileChannel channel = null;
        try{
            file = new RandomAccessFile(fsimageFile, "rw");
            out = new FileOutputStream(file.getFD());
            channel = out.getChannel();
            channel.write(buffer);
            channel.force(false);
        } finally {
            if(out != null){
                out.close();
            }
            if(file != null){
                file.close();
            }
            if(channel != null){
                channel.close();
            }
        }
    }

    /**
     * 删除上一个fsimage磁盘文件
     */
    private void removeLastFsimageFile(){
        File file = new File(lastFImageFile);
        if(file.exists()){
            file.delete();
        }
    }
}

package com.ybh.dfs.backupnode.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    public static final Integer CHECKPOINT_INTERVAL = 1 * 60 * 1000;

    private BackupNode backupNode;
    private FSNamesystem namesystem;
    private NameNodeRpcClient namenode;
    private String lastFSImageFile = "";
    private long checkpointTime = System.currentTimeMillis();

    public FsImageCheckpointer(BackupNode backupNode, FSNamesystem namesystem, NameNodeRpcClient namenode) {
        this.backupNode = backupNode;
        this.namesystem = namesystem;
        this.namenode = namenode;
    }

    @Override
    public void run() {
        System.out.println("fsimage checkpoint 定时调度线程启动......");

        while(backupNode.isRunning()){
            try{
                if(!namesystem.isFinishedRecover()) {
                    System.out.println("当前还没完成元数据恢复，不进行checkpoint......");
                    Thread.sleep(1000);
                    continue;
                }

                if(lastFSImageFile.equals("")){
                    this.lastFSImageFile = namesystem.getCheckpointFile();
                }

                long now = System.currentTimeMillis();

                if(now - checkpointTime > CHECKPOINT_INTERVAL) {
                    if(!namenode.getNammenodeRunning()){
                        System.out.println("namenode当前无法访问, 不执行 checkpoint......");
                        continue;
                    }
                    // 触发checkpoint操作
                    // 写数据的过程中，你必须是
                    System.out.println("准备执行checkpoint操作，写入fsimage文件。。。。。。");
                    doCheckpoint();
                    System.out.println("完成checkpoint操作。。。。。。");
                }

                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 将fsimage 持久化到磁盘上
     * @throws Exception
     */
    private void doCheckpoint() throws Exception {
        FSImage fsimage = namesystem.getFSImage();
        removeLastFSimageFile();
        writeFSImageFile(fsimage);
        uploadFSImageFile(fsimage);
        updateCheckpointTxid(fsimage);
        saveCheckpointInfo(fsimage);
        this.checkpointTime = System.currentTimeMillis();
    }

    /**
     * 持久化checkpoint信息
     * @param fsimage
     */
    private void saveCheckpointInfo(FSImage fsimage) {
        String path = "D:\\dfs-test\\backupnode\\checkpoint-info.meta";

        RandomAccessFile raf = null;
        FileOutputStream out = null;
        FileChannel channel = null;
        try{
            File file = new File(path);
            if(file.exists()){
                file.delete();
            }
            long now = System.currentTimeMillis();
            this.checkpointTime = now;
            long checkpointTxid = fsimage.getMaxTxid();
            ByteBuffer buffer = ByteBuffer.wrap((now + "_" + checkpointTxid + "_" + lastFSImageFile).getBytes());

            raf = new RandomAccessFile(path, "rw");
            out = new FileOutputStream(raf.getFD());
            channel = out.getChannel();

            channel.write(buffer);
            channel.force(false);

            System.out.println("checkpoint信息持久化到磁盘文件");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(out != null){
                    out.close();
                }
                if(raf != null){
                    raf.close();
                }
                if(channel != null){
                    channel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 更新checkpoint txid
     * @param fsimage
     */
    private void updateCheckpointTxid(FSImage fsimage) {
        namenode.updateCheckpointTxid(fsimage.getMaxTxid());
    }


    /**
     * 删除上一个fsimage磁盘文件
     */
    private void removeLastFSimageFile(){
        File file = new File(lastFSImageFile);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 写入最新的 fsimage 文件
     * @throws Exception
     */
    private void writeFSImageFile(FSImage fsimage) throws Exception{
        ByteBuffer buffer = ByteBuffer.wrap(fsimage.getFsimageJson().getBytes());

        // fsimage 文件名的格式，包含当前最后一个edits.log txid
        String fsimageFile = "D:\\dfs-test\\backupnode\\fsimage-" + (fsimage.getMaxTxid()) + ".meta";
        lastFSImageFile = fsimageFile;

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
     * 上传fsimage文件
     * @param fsImage
     * @throws Exception
     */
    private void uploadFSImageFile(FSImage fsImage) throws Exception{
        FSImageUploader fsImageUploader = new FSImageUploader(fsImage);
        fsImageUploader.start();
    }
}

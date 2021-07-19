package com.ybh.dfs.backupnode.server;

/**
 * 同步editslog进程
 */
public class BackupNode {
    private volatile  Boolean isRunning = true;
    private FSNamesystem namesystem;
    private NameNodeRpcClient namenode;

    public static void main(String[] args) throws InterruptedException {
        BackupNode backupNode = new BackupNode();
        backupNode.init();
        backupNode.start();
    }

    public void init(){
        this.namesystem = new FSNamesystem();
        this.namenode = new NameNodeRpcClient();
    }

    private void start()  {
        EditLogFetcher editLogFetcher = new EditLogFetcher(this, namesystem, this.namenode);
        editLogFetcher.start();

        FsImageCheckpointer fsImageCheckpointer = new FsImageCheckpointer(this, namesystem, namenode);
        fsImageCheckpointer.start();

    }

    public void run() throws InterruptedException {
        while (isRunning){
            Thread.sleep(1000);
        }
    }

    public Boolean isRunning(){
        return isRunning;
    }


}

package com.ybh.dfs.datanode.server;

import java.io.File;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;

/**
 * DataNode启动类
 * @author zhonghuashishan
 *
 */
public class DataNode {

	/**
	 * 是否还在运行
	 */
	private volatile Boolean shouldRun;
	/**
	 * 负责跟一组NameNode通信的组件
	 */
	private NameNodeRpcClient nameNodeRpcClient;

	/**
	 * 心跳管理组件
	 */
	private HeartBeatManager heartBeatManager;
	/**
	 * 磁盘管理组件
	 */
	private StorageManager storageManager;

	/**
	 * 初始化DataNode
	 */
	public DataNode() throws InterruptedException {
		this.shouldRun = true;
		this.nameNodeRpcClient = new NameNodeRpcClient();
		Boolean registerResult = this.nameNodeRpcClient.register();
		/**
		 * 如果注册成功
		 */
		this.storageManager = new StorageManager();
		if(registerResult){
			StorageInfo storageInfo = storageManager.getStorageInfo();
			this.nameNodeRpcClient.reportCompleteStorageInfo(storageInfo);
		} else {
			System.out.println("不需要全量上报存储信息......");
		}

		this.heartBeatManager = new HeartBeatManager(nameNodeRpcClient, storageManager);
		this.heartBeatManager.start();

		DataNodeNIOServer nioServer = new DataNodeNIOServer(nameNodeRpcClient);
		nioServer.start();
	}

	/**
	 * 运行DataNode
	 */
	private void start() {
		try {
			while(shouldRun) {
				Thread.sleep(1000);  
			}   
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception{
		DataNode datanode = new DataNode();
		datanode.start();
	}
	
}

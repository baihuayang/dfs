package com.ybh.dfs.namenaode.server;

/**
 * NameNode核心启动类
 * @author zhonghuashishan
 *
 */
public class NameNode {

	/**
	 * NameNode是否在运行
	 */
	private volatile Boolean shouldRun;
	/**
	 * 负责管理元数据的核心组件：管理的是一些文件目录树，支持权限设置
	 */
	private FSNamesystem namesystem;
	/**
	 * 负责管理集群中所有的Datanode的组件
	 */
	private DataNodeManager datanodeManager;
	/**
	 * NameNode对外提供rpc接口的server，可以响应请求
	 */
	private NameNodeRpcServer rpcServer;

	/**
	 *  接收 backupnode 上传
	 */
	private FSImageUploadServer fsImageUploadServer;
	

	/**
	 * 初始化NameNode
	 */
	public NameNode()  {
		this.datanodeManager = new DataNodeManager();
		this.namesystem = new FSNamesystem(datanodeManager);
		datanodeManager.setFsNamesystem(namesystem);
		this.rpcServer = new NameNodeRpcServer(this.namesystem, this.datanodeManager);
		this.fsImageUploadServer = new FSImageUploadServer();

	}
	
	/**
	 * 让NameNode运行起来
	 */
	private void start() throws Exception{
		this.fsImageUploadServer.start();
		this.rpcServer.start();
		this.rpcServer.blockUntilShutdown();
	}
		
	public static void main(String[] args) throws Exception {		
		NameNode namenode = new NameNode();
		namenode.start();
	}
}

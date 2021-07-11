package com.ybh.dfs.namenaode.server;

import java.io.IOException;

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
	
	public NameNode() {
		this.shouldRun = true;
	}
	
	/**
	 * 初始化NameNode
	 */
	private void initialize() throws IOException, InterruptedException {
		this.namesystem = new FSNamesystem();
		this.datanodeManager = new DataNodeManager();
		this.rpcServer = new NameNodeRpcServer(this.namesystem, this.datanodeManager);


	}
	
	/**
	 * 让NameNode运行起来
	 */
	private void start() {
		try {
			while(shouldRun) {
				this.rpcServer.start();
				this.rpcServer.blockUntilShutdown();
			}  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void main(String[] args) throws Exception {		
		NameNode namenode = new NameNode();
		namenode.initialize();
		namenode.start();
	}
	
}

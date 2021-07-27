package com.ybh.dfs.namenaode.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个组件，就是负责管理集群里的所有的datanode的
 * @author zhonghuashishan
 *
 */
public class DataNodeManager {

	private Map<String, DataNodeInfo> datanodes =
			new ConcurrentHashMap<String, DataNodeInfo>();

	public DataNodeManager(){
		new DataNodeAliveMonitor().start();
	}

	private FSNamesystem namesystem;

	public void setFsNamesystem(FSNamesystem fsNamesystem) {
		this.namesystem = fsNamesystem;
	}

	public void createLostReplicaTask(DataNodeInfo deadDataNode) {
		System.out.println("wocacacaca");
		List<String> files = namesystem.getFilesByDatanode(deadDataNode.getIp(), deadDataNode.getHostname());

		for(String file : files) {
			String filename = file.split("_")[0];
			Long fileLength = Long.valueOf(file.split("_")[1]);
			// 源头复制数据节点
			DataNodeInfo sourceDataNode = namesystem.getReplicateSource(filename, deadDataNode);
			// 目标复制数据节点
			DataNodeInfo destDataNode = allocateReplicateDataNodes(fileLength, sourceDataNode, deadDataNode); // todo 如果这个目标数据节点 有丢失的副本呢？
			//复制任务
			ReplicateTask replicateTask = new ReplicateTask(filename, fileLength, sourceDataNode, destDataNode);
			destDataNode.addReplicaTask(replicateTask);
			System.out.println("为目标数据节点生成一个副本复制任务," + replicateTask);
		}
	}



	/**
	 * datanode进行注册
	 * @param ip 
	 * @param hostname
	 */
	public Boolean register(String ip, String hostname, int nioPort) {
		if(datanodes.containsKey(ip + "-" + hostname)){
			System.out.println("注册失败，当前已经存在datanode......");
			return false;
		}
		DataNodeInfo datanode = new DataNodeInfo(ip, hostname, nioPort);
		datanodes.put(ip + "-" + hostname, datanode);
		System.out.println("DataNode 注册 ip=" +ip + " hostname=" + hostname + ", nioPort=" + nioPort);
		return true;
	}
	
	/**
	 * datanode进行心跳
	 * @param ip
	 * @param hostname
	 * @return
	 */
	public Boolean heartbeat(String ip, String hostname) {
		DataNodeInfo datanode = datanodes.get(ip + "-" + hostname);
		if(datanode == null) {
			System.out.println("心跳失败, 需要重新注册......");
			return false;
		}
		datanode.setLatestHeartbeatTime(System.currentTimeMillis());
		System.out.println("DataNode 心跳 ip=" +ip + " hostname=" + hostname);

		return true;
	}

	/**
	 * 分配双副本数据节点
	 * @param fileSize
	 * @return
	 */
	public List<DataNodeInfo> allocateDataNodes(long fileSize) {
		synchronized (this) {
			// 取出来所有的datanode，并且按照存储数据大小进行排序
			List<DataNodeInfo> dataNodeList = new ArrayList<>();
			for(DataNodeInfo datanode : datanodes.values()){
				dataNodeList.add(datanode);
			}
			Collections.sort(dataNodeList);
			// 选择存储数据最少的头两个 datanode 出来
			List<DataNodeInfo> selectedDatanodes = new ArrayList<>();
			if(dataNodeList.size() >= 2){
				selectedDatanodes.add(dataNodeList.get(0));
				selectedDatanodes.add(dataNodeList.get(1));
				// 默认认为：要上传的文件会被放到那两个datanode上去
				// 更新那两个 datanode 存储数据的大小，加上上传文件的大小
				dataNodeList.get(0).addStoredDataSize(fileSize);
				dataNodeList.get(1).addStoredDataSize(fileSize);
			}

			return selectedDatanodes;
		}
	}

	/**
	 * 分配双副本数据节点
	 * @param fileSize
	 * @return
	 */
	public DataNodeInfo allocateReplicateDataNodes(long fileSize, DataNodeInfo sourceDatanode, DataNodeInfo deadDataNode) {
		synchronized (this) {
			// 取出来所有的datanode，并且按照存储数据大小进行排序
			List<DataNodeInfo> dataNodeList = new ArrayList<>();
			for(DataNodeInfo datanode : datanodes.values()){
				if(!datanode.equals(sourceDatanode) && !datanode.equals(deadDataNode)) {
					dataNodeList.add(datanode);
				}
			}
			Collections.sort(dataNodeList);
			// 选择存储数据最少的头两个 datanode 出来
			DataNodeInfo selectedDatanodes = null;
			if(!dataNodeList.isEmpty()){
				selectedDatanodes = dataNodeList.get(0);
				dataNodeList.get(0).addStoredDataSize(fileSize);
			}

			return selectedDatanodes;
		}
	}

	/**
	 * 设置一个 datanode 的存储数据大小
	 * @param ip
	 * @param hostname
	 * @param storedDataSize
	 */
	public void setDataNodeStoredDataSize(String ip, String hostname, Long storedDataSize) {
		DataNodeInfo datanode = datanodes.get(ip + "-" + hostname);
		datanode.setStorageDataSize(storedDataSize);
	}

	/**
	 * 获取datanode 信息
	 * @param ip
	 * @param hostname
	 * @return
	 */
	public DataNodeInfo getDatanode(String ip, String hostname) {
		return datanodes.get(ip + "-" + hostname);
	}

	/**
	 * 是否存活监控线程
	 */
	class DataNodeAliveMonitor extends Thread {
		@Override
		public void run() {
			try{
				while(true){
					List<DataNodeInfo> toRemoveDatanodes = new ArrayList<DataNodeInfo>();
					Iterator<DataNodeInfo> dataNodeInfoIterator = datanodes.values().iterator();
					DataNodeInfo dataNode = null;
					while(dataNodeInfoIterator.hasNext()){
						dataNode = dataNodeInfoIterator.next();
						if(System.currentTimeMillis() - dataNode.getLatestHeartbeatTime() > 1 * 60 * 1000){
							toRemoveDatanodes.add(dataNode);
						}
					}
					if(!toRemoveDatanodes.isEmpty()){
						for(DataNodeInfo toRemoveDatanode : toRemoveDatanodes){
							System.out.println("数据节点【" + toRemoveDatanode + "】宕机，需要进行副本复制......");
							createLostReplicaTask(toRemoveDatanode);
							datanodes.remove(toRemoveDatanode.getId());
							System.out.println("从内存数据结构中删除掉这个数据节点," + datanodes);
							namesystem.removeDeadDatanode(toRemoveDatanode);
						}
					}

					Thread.sleep(30 *1000);
				}

			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}

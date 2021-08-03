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

	public void createReplicaTask(DataNodeInfo deadDataNode) {
		synchronized (this) {
			System.out.println("wocacacaca");
			List<String> files = namesystem.getFilesByDatanode(deadDataNode.getIp(), deadDataNode.getHostname());

			for(String file : files) {
				String filename = file.split("_")[0];
				Long fileLength = Long.valueOf(file.split("_")[1]);
				// 源头复制数据节点
				DataNodeInfo sourceDataNode = namesystem.getReplicateSource(filename, deadDataNode);
				// 目标复制数据节点
				DataNodeInfo destDataNode = allocateReplicateDataNodes(fileLength, sourceDataNode, deadDataNode);
				//复制任务
				ReplicateTask replicateTask = new ReplicateTask(filename, fileLength, sourceDataNode, destDataNode);
				destDataNode.addReplicaTask(replicateTask);
				System.out.println("为目标数据节点生成一个副本复制任务," + replicateTask);
			}
		}
	}

	/**
	 * 创建平衡datanode 节点复制任务
	 */
	public void createRebalanceReplicateTasks() {
		synchronized (this) {
			long total = 0L;
			for(DataNodeInfo dataNodeInfo : datanodes.values()) {
				total += dataNodeInfo.getStorageDataSize();
			}
			long average = total / datanodes.size();
			List<DataNodeInfo> sourceDataNodeList = new ArrayList<>();
			List<DataNodeInfo> destDataNodeList = new ArrayList<>();
			for(DataNodeInfo dataNodeInfo : datanodes.values()) {
				if(dataNodeInfo.getStorageDataSize() > average) {
					sourceDataNodeList.add(dataNodeInfo);
				}
				if(dataNodeInfo.getStorageDataSize() < average) {
					destDataNodeList.add(dataNodeInfo);
				}
			}
			List<RemoveReplicaTask> removeReplicaTasks = new ArrayList<>();
			for(DataNodeInfo sourceDataNode : sourceDataNodeList) {
				long toRemoveSize = sourceDataNode.getStorageDataSize() - average;
				for(DataNodeInfo destDataNode : destDataNodeList) {
					// 直接一次性放到一台机器
					if(destDataNode.getStorageDataSize() + toRemoveSize <= average) {
						long removedDataSize = 0L;
						createRebalanceTask(sourceDataNode, destDataNode,
								removeReplicaTasks, removedDataSize);
						break;
					}
					// 只能把部分数据放到这里
					if(destDataNode.getStorageDataSize() + toRemoveSize > average) {
						long maxRemoveDataSize = average - destDataNode.getStorageDataSize();
						long removedDataSize = createRebalanceTask(sourceDataNode, destDataNode,
								removeReplicaTasks, maxRemoveDataSize);
						toRemoveSize -= removedDataSize;
					}
				}
			}
			new DelayRemoveReplicasTask(removeReplicaTasks).start();
		}
	}


	private long createRebalanceTask(DataNodeInfo sourceDataNode, DataNodeInfo destDataNode,
									 List<RemoveReplicaTask> removeReplicaTasks, long maxRemoveDataSize) {
		long removedDataSize = 0L;
		List<String> files =
				namesystem.getFilesByDatanode(destDataNode.getIp(), destDataNode.getHostname());
		for(String file : files) {
			String filename = file.split("_")[0];
			Long fileLength = Long.parseLong(file.split("_")[1]);

			if(removedDataSize >= maxRemoveDataSize) {
				break;
			}
			//为文件生成复制任务
			ReplicateTask replicateTask = new ReplicateTask(filename, fileLength, sourceDataNode, destDataNode);
			destDataNode.addReplicaTask(replicateTask);
			destDataNode.addStoredDataSize(fileLength);

			//为文件生成删除任务
			sourceDataNode.addStoredDataSize(-fileLength);
			namesystem.removeReplicasFromDataNode(sourceDataNode.getId(), file);
			//生成副本复制任务
			RemoveReplicaTask removeReplicaTask = new RemoveReplicaTask(filename, sourceDataNode);
			removeReplicaTasks.add(removeReplicaTask);

			removedDataSize += fileLength;
		}
		return removedDataSize;
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
    public DataNodeInfo reallocateDataNode (long fileSize, String excludeDataNodeId) {
        synchronized (this) {
            // 取出来所有的datanode，并且按照存储数据大小进行排序
            DataNodeInfo excludeDataNode = datanodes.get(excludeDataNodeId);
            excludeDataNode.addStoredDataSize(-fileSize);

            List<DataNodeInfo> dataNodeList = new ArrayList<>();
            for(DataNodeInfo datanode : datanodes.values()){
                if(!excludeDataNode.equals(datanode)){
                    dataNodeList.add(datanode);
                }
            }
            Collections.sort(dataNodeList);
            // 选择存储数据最少的头两个 datanode 出来
            DataNodeInfo selectDatanode = null;
            if(dataNodeList.size() >= 1){
                // 默认认为：要上传的文件会被放到那两个datanode上去
                // 更新那两个 datanode 存储数据的大小，加上上传文件的大小
                selectDatanode = dataNodeList.get(0);
                dataNodeList.get(0).addStoredDataSize(fileSize);
            }

            return selectDatanode;
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

	public DataNodeInfo getDatanode(String id) {
		return datanodes.get(id);
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
							createReplicaTask(toRemoveDatanode);
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

class DelayRemoveReplicasTask  extends Thread{
	List<RemoveReplicaTask> removeReplicaTasks;

	public DelayRemoveReplicasTask(List<RemoveReplicaTask> removeReplicaTasks) {
		this.removeReplicaTasks = removeReplicaTasks;
	}

	@Override
	public void run() {
		try{
			Thread.sleep(24 * 60 * 60 * 1000);
			for(RemoveReplicaTask removeReplicaTask : removeReplicaTasks) {
				removeReplicaTask.getDataNode().addRemoveReplicaTask(removeReplicaTask);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}

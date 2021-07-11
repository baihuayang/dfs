package com.ybh.dfs.namenaode.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	/**
	 * datanode进行注册
	 * @param ip 
	 * @param hostname
	 */
	public Boolean register(String ip, String hostname) {
		DataNodeInfo datanode = new DataNodeInfo(ip, hostname);
		datanodes.put(ip + "-" + hostname, datanode);
		System.out.println("DataNode 注册 ip=" +ip + " hostname=" + hostname);
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
		datanode.setLatestHeartbeatTime(System.currentTimeMillis());
		System.out.println("DataNode 心跳 ip=" +ip + " hostname=" + hostname);

		return true;
	}

	/**
	 * 是否存活监控线程
	 */
	class DataNodeAliveMonitor extends Thread {
		@Override
		public void run() {
			try{
				while(true){
					List<String> toRemoveDatanodes = new ArrayList<String>();
					Iterator<DataNodeInfo> dataNodeInfoIterator = datanodes.values().iterator();
					DataNodeInfo dataNode = null;
					while(dataNodeInfoIterator.hasNext()){
						dataNode = dataNodeInfoIterator.next();
						if(System.currentTimeMillis() - dataNode.getLatestHeartbeatTime() > 90 * 1000){
							toRemoveDatanodes.add(dataNode.getIp() + "-" + dataNode.getHostname());
						}
					}
					if(!toRemoveDatanodes.isEmpty()){
						for(String toRemoveDatanode : toRemoveDatanodes){
							datanodes.remove(toRemoveDatanode);
						}
					}

					Thread.sleep(30 *1000);
				}

			}catch (Exception e){

			}
		}
	}
}

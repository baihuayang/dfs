package com.ybh.dfs.namenaode.server;

import com.sun.jmx.snmp.tasks.TaskServer;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 用来描述datanode的信息
 * @author zhonghuashishan
 *
 */
public class DataNodeInfo implements Comparable<DataNodeInfo>{

	/**
	 * ip地址
	 */
	private String ip;
	/**
	 * 主机名
	 */
	private String hostname;

	/**
	 * nio端口
	 */
	private int nioPort;
	/**
	 * 最近心跳时间
	 */
	private long latestHeartbeatTime;
	/**
	 * 已经存储数据大小
	 */
	private long storageDataSize = 0L;

	private ConcurrentLinkedQueue<ReplicateTask> replicaTaskQueue =
			new ConcurrentLinkedQueue<>();

	private ConcurrentLinkedQueue<RemoveReplicaTask> removeReplicaTaskQueue =
			new ConcurrentLinkedQueue<>();
	
	public DataNodeInfo(String ip, String hostname, int nioPort) {
		this.ip = ip;
		this.hostname = hostname;
		this.nioPort = nioPort;
		this.latestHeartbeatTime = System.currentTimeMillis();
	}
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public long getLatestHeartbeatTime() {
		return latestHeartbeatTime;
	}
	public void setLatestHeartbeatTime(long latestHeartbeatTime) {
		this.latestHeartbeatTime = latestHeartbeatTime;
	}

	public long getStorageDataSize() {
		return storageDataSize;
	}

	public void setStorageDataSize(long storageDataSize) {
		this.storageDataSize = storageDataSize;
	}

	public void addStoredDataSize(long storedDataSize) {
		this.storageDataSize += storedDataSize;
	}

	public int getNioPort() {
		return nioPort;
	}

	public void setNioPort(int nioPort) {
		this.nioPort = nioPort;
	}

	public void addReplicaTask(ReplicateTask replicateTask) {
		replicaTaskQueue.add(replicateTask);
	}

	public ReplicateTask pollReplicaTask() {
		ReplicateTask replicateTask = null;
		if(!replicaTaskQueue.isEmpty()) {
			replicateTask = replicaTaskQueue.poll();
		}
		return replicateTask;
	}

	public void addRemoveReplicaTask(RemoveReplicaTask replicateTask) {
		removeReplicaTaskQueue.add(replicateTask);
	}

	public RemoveReplicaTask pollRemoveReplicaTask() {
		RemoveReplicaTask removeReplicaTask = null;
		if(!removeReplicaTaskQueue.isEmpty()) {
			removeReplicaTask = removeReplicaTaskQueue.poll();
		}
		return removeReplicaTask;
	}

	@Override
	public int compareTo(DataNodeInfo o) {
		return (int) (this.storageDataSize - o.storageDataSize);
	}

	@Override
	public String toString() {
		return "DataNodeInfo{" +
				"ip='" + ip + '\'' +
				", hostname='" + hostname + '\'' +
				", nioPort=" + nioPort +
				", latestHeartbeatTime=" + latestHeartbeatTime +
				", storageDataSize=" + storageDataSize +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DataNodeInfo that = (DataNodeInfo) o;
		return Objects.equals(ip, that.ip) && Objects.equals(hostname, that.hostname);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip, hostname);
	}

	public String getId() {
		return ip + "-" + hostname;
	}
}

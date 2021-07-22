package com.ybh.dfs.namenaode.server;

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
}

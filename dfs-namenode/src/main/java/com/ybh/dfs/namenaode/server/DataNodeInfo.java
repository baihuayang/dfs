package com.ybh.dfs.namenaode.server;

/**
 * 用来描述datanode的信息
 * @author zhonghuashishan
 *
 */
public class DataNodeInfo {

	/**
	 * ip地址
	 */
	private String ip;
	/**
	 * 主机名
	 */
	private String hostname;
	/**
	 * 最近心跳时间
	 */
	private long latestHeartbeatTime;
	/**
	 * 已经存储数据大小
	 */
	private long storageDataSize = 0L;
	
	public DataNodeInfo(String ip, String hostname) {
		this.ip = ip;
		this.hostname = hostname;
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
	
}

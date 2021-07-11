package com.ybh.dfs.namenaode.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ybh.dfs.namenode.rpc.model.*;
import com.ybh.dfs.namenode.rpc.service.NameNodeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * NameNode的rpc服务的接口
 * @author zhonghuashishan
 *
 */
public class NameNodeServiceImpl implements NameNodeServiceGrpc.NameNodeService {

	public static final Integer STATUS_SUCCESS = 1;
	public static final Integer STATUS_FAILED = 2;
	public static final Integer STATUS_SHUTDOWN = 3;

	public static final Integer BACKUP_NODE_FETCH_SIZE = 10;

	/**
	 * 负责管理元数据的核心组件
	 */
	private FSNamesystem namesystem;
	/**
	 * 负责管理集群中所有的datanode的组件
	 */
	private DataNodeManager datanodeManager;

	/**
	 * 是否还在运行
	 */
	private volatile Boolean isRunning = true;

	/**
	 * 当前backupnode 同步到哪一条
	 */
	private long syncedTxid = 0L;

	/**
	 * 当前缓冲的一小部分editlog
	 */
	private JSONArray currentBufferedEditsLog = new JSONArray();
	/**
	 * 当前缓存里的 editslog 最大的一个txid
	 */
	private long currentBufferedMaxTxid = 0L;

	/**
	 * 当前内存里缓冲了哪个磁盘文件的数据
	 */
	private String bufferedFlushedTxid;

	public NameNodeServiceImpl(
			FSNamesystem namesystem,
			DataNodeManager datanodeManager) {
		this.namesystem = namesystem;
		this.datanodeManager = datanodeManager;
	}
	
	/**
	 * 创建目录
	 * @param path 目录路径
	 * @return 是否创建成功
	 * @throws Exception
	 */
	public Boolean mkdir(String path) throws Exception {
		return this.namesystem.mkdir(path);
	}
	
	/**
	 * datanode进行注册
	 * @param ip
	 * @param hostname
	 * @return
	 * @throws Exception
	 */
	public Boolean register(String ip, String hostname) throws Exception {
		return datanodeManager.register(ip, hostname);
	}
	

	public Boolean heartbeat(String ip, String hostname) throws Exception {
		return datanodeManager.heartbeat(ip, hostname);
	}
	
	/**
	 * 启动这个rpc server
	 */
	public void start() {
		System.out.println("开始监听指定的rpc server的端口号，来接收请求");  
	}


	/**
	 * datanode进行注册
	 * @return
	 * @throws Exception
	 */
	@Override
	public void register(RegisterRequest registerRequest,
						 StreamObserver<RegisterResponse> streamObserver) {
		datanodeManager.register(registerRequest.getIp(), registerRequest.getHostname());
		RegisterResponse response = RegisterResponse.newBuilder()
				.setStatus(STATUS_SUCCESS)
				.build();
		streamObserver.onNext(response);
		streamObserver.onCompleted();
	}

	/**
	 * datanode进行心跳
	 * @return
	 * @throws Exception
	 */
	@Override
	public void heartbeat(HeartbeatRequest heartbeatRequest,
						  StreamObserver<HeartbeatResponse> streamObserver) {
		datanodeManager.heartbeat(heartbeatRequest.getIp(), heartbeatRequest.getHostname());
		HeartbeatResponse response = HeartbeatResponse.newBuilder()
				.setStatus(STATUS_SUCCESS)
				.build();
		streamObserver.onNext(response);
		streamObserver.onCompleted();
	}

	/**
	 * 创建目录
	 * @param mkdirRequest
	 * @param streamObserver
	 */
	@Override
	public void mkdir(MkdirRequest mkdirRequest, StreamObserver<MkdirResponse> streamObserver) {
		try {
			MkdirResponse response = null;
			if(!isRunning){
				response = MkdirResponse.newBuilder()
						.setStatus(STATUS_SHUTDOWN)
						.build();
			} else{
				System.out.println("(mkdirRequest.getPath(): " + mkdirRequest.getPath());
				this.namesystem.mkdir(mkdirRequest.getPath());
				System.out.println("创建目录 path=" + mkdirRequest.getPath());
				response = MkdirResponse.newBuilder()
						.setStatus(STATUS_SUCCESS)
						.build();
			}
			streamObserver.onNext(response);
			streamObserver.onCompleted();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 优雅关闭
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void shutdown(ShutdownRequest request, StreamObserver<ShutdownResponse> responseObserver) {
		this.isRunning = false;
		this.namesystem.flush();
	}

	/**
	 * 拉去editlog
	 * @param request
	 * @param responseObserver
	 */
	@Override
	public void fetchEditsLog(FetchEditsLogRequest request, StreamObserver<FetchEditsLogResponse> responseObserver) {
		FetchEditsLogResponse response = null;
		JSONArray fetchedEditsLog = new JSONArray();
		//todo flushedTxids 是否只记录最大的即可？ 因为磁盘txid是顺序的？
		List<String> flushedTxids = namesystem.getEditsLog().getFlushedTxids();
		//如果此时还没有刷出来任何磁盘文件的话，那么此时数据仅仅存在于内存缓冲
		if(flushedTxids.size() == 0){
			System.out.println("从内存拉取。。。。。。。。");
			// 如果之前拉取过数据
			fetchFromBufferEditLog(fetchedEditsLog);
		}
		// 如果此时有落地磁盘的文件了，扫描所有磁盘文件的索引范围
		else {
			// 第一种情况，你要拉取的txid是在某个磁盘文件里的
			// 有磁盘文件，而且内存里还缓存了某个磁盘文件的数据
			if(bufferedFlushedTxid != null){
				// 如果拉取文件就在当前缓存数据里bufferedFlushedTxid
				if(existInFlushedFile(bufferedFlushedTxid)){
					System.out.println("上一次已经缓存过磁盘文件的数据，直接从磁盘文件缓存中拉取editslog。。。。。。。");
					fetchFromCurrentBuffer(fetchedEditsLog);
				}
				// 如果拉取文件就不在当前缓存数据里
				else {
					System.out.println("上一次已经缓存过磁盘文件找不到，从下一个磁盘文件缓存中拉取editslog。。。。。。。");
					String nextFlushedTxid = getNextFlushedTxid(flushedTxids, bufferedFlushedTxid);
					//如果可以找到下一个磁盘文件，那么就从下一个磁盘文件读取数据
					if(nextFlushedTxid != null){
						fetchFromFlushedFile(nextFlushedTxid, fetchedEditsLog);
					}
					// 没有找到下一个磁盘文件
					else {
						System.out.println("上一次缓存的磁盘文件找不到要拉取的数据，而且没有下一个磁盘文件");
						fetchFromBufferEditLog(fetchedEditsLog);
					}
				}
			} else {
				// 遍历所有磁盘文件的索引范围
				System.out.println("第一次从磁盘文件拉取。。。。。。。");
				Boolean fetchedFromFlushedFile = false;

				for(String flushedTxid : flushedTxids){
					if (existInFlushedFile(flushedTxid)) {
						System.out.println("从磁盘文件拉取editslog。。。。。。。flushedTxid=" + flushedTxid);
						fetchFromFlushedFile(flushedTxid, fetchedEditsLog);
						fetchedFromFlushedFile = true;
						break;
					}
				}

				// 第二种情况，你要拉去的txid已经比磁盘文件里的全部都新
				if(!fetchedFromFlushedFile) {
					System.out.println("所有磁盘文件都没找到要拉取的editslog，直接从内存缓冲中拉取");
					fetchFromBufferEditLog(fetchedEditsLog);
				}
			}

		}
		response = FetchEditsLogResponse.newBuilder()
				.setEditsLog(fetchedEditsLog.toJSONString())
				.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	/**
	 * 获取下一个磁盘文件对应txid范围
	 * @param flushedTxids
	 * @param bufferedFlushedTxid
	 * @return
	 */
	private String getNextFlushedTxid(List<String> flushedTxids, String bufferedFlushedTxid) {
		for(int i=0;i<flushedTxids.size();i++){
			if(flushedTxids.get(i).equals(bufferedFlushedTxid)){
				if(i+1 < flushedTxids.size()){
					return flushedTxids.get(i+1);
				}
			}
		}
		return null;
	}

	/**
	 * 从已经刷入磁盘的文件里读取edits.log，同时缓存这个文件数据到内存
	 * @param flushedTxid
	 */
	private void fetchFromFlushedFile(String flushedTxid, JSONArray fetchedEditsLog){
		try {
			//此时，可以把磁盘文件里以及下一个磁盘文件数据的数据都读取出来，放入缓存
			String[] flushedTxidSplited = flushedTxid.split("_");

			long startTxid = Long.valueOf(flushedTxidSplited[0]);
			long endTxid = Long.valueOf(flushedTxidSplited[1]);

			String currentEditsLogFile = "D:\\dfs-test\\namenode\\dfs-edits-" + startTxid + "-" + endTxid + ".log";

			List<String> editsLogs = Files.readAllLines(Paths.get(currentEditsLogFile),
					StandardCharsets.UTF_8);
			currentBufferedEditsLog.clear();
			for(String editsLog : editsLogs){
				currentBufferedEditsLog.add(JSONObject.parseObject(editsLog));
				currentBufferedMaxTxid = JSONObject.parseObject(editsLog).getLongValue("txid");
			}
			bufferedFlushedTxid = flushedTxid; //缓存了某个刷融入磁盘文件的数据

			fetchFromCurrentBuffer(fetchedEditsLog);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 是否存在于刷到磁盘文件中
	 * @param flushedTxid
	 * @return
	 */
	private Boolean existInFlushedFile(String flushedTxid) {
		String[] flushedTxidSplited = flushedTxid.split("_");

		long startTxid = Long.valueOf(flushedTxidSplited[0]);
		long endTxid = Long.valueOf(flushedTxidSplited[1]);
		long fetchTxid = syncedTxid + 1;
		return fetchTxid >= startTxid && fetchTxid <= endTxid;
	}

	/**
	 * 从内存缓冲的editsLog中 拉取数据
	 * @param fetchedEditsLog
	 */
	private void fetchFromBufferEditLog(JSONArray fetchedEditsLog) {
		//如果要拉取的txid还在上一次内存缓存中，继续缓存中拉取
		long fetchTxid = syncedTxid + 1;
		if(fetchTxid <= currentBufferedMaxTxid){
			System.out.println("尝试从内存缓冲拉取的时候，发现上一次内存缓存有数据可供拉取。。。。。。");
			fetchFromCurrentBuffer(fetchedEditsLog);
			return;
		}

		currentBufferedEditsLog.clear();

		//全部都在内存缓冲里
		String[] bufferedEditsLog = namesystem.getEditsLog().getBufferedEditsLog();
		if(bufferedEditsLog != null){
			for(String editsLog : bufferedEditsLog){
				currentBufferedEditsLog.add(JSONObject.parseObject(editsLog));
				//记录一下当前内存缓存中的数据 最大txid是多少，下次可以判断，是否需要重新读取
				currentBufferedMaxTxid = JSONObject.parseObject(editsLog).getLongValue("txid");
			}
			bufferedFlushedTxid = null;

			fetchFromCurrentBuffer(fetchedEditsLog);
		}
	}

	/**
	 * 从当前已经在内存里缓存的数据中拉取editslog
	 * @param fetchedEditsLog
	 */
	private void fetchFromCurrentBuffer(JSONArray fetchedEditsLog) {
		int fetchCount = 0;
		for(int i=0; i<currentBufferedEditsLog.size(); i++){
			if(currentBufferedEditsLog.getJSONObject(i).getLong("txid") == syncedTxid + 1){
				fetchedEditsLog.add(currentBufferedEditsLog.getJSONObject(i));
				syncedTxid = currentBufferedEditsLog.getJSONObject(i).getLong("txid");
				fetchCount++;
			}
			if(fetchCount == BACKUP_NODE_FETCH_SIZE){
				break;
			}
		}
	}
}

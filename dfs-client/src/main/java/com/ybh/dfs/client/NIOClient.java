package com.ybh.dfs.client;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * 客户端的一个 NIOClient
 */
public class NIOClient {

	private NetworkManager networkManager = new NetworkManager();

	/**
	 * 发送一个文件过去
	 */
	public Boolean sendFile( FileInfo fileInfo, Host host, ResponseCallback callback) {
//		try{
			//按理说，根据hostname检查一下，跟对方机器连接是否建立好了，如果没有建立好，那么直接在这里建立，
			// 否则新建一个并缓存，已备下次进行使用
			if(!networkManager.maybeConnect(host.getHostname(), host.getNioPort())){
				return false;
			}
			NetworkRequest sendFileRequest = createSendFileRequest(fileInfo, host,  callback);
			networkManager.sendRequest(sendFileRequest);
		return true;
	}

	private NetworkRequest createSendFileRequest(FileInfo fileInfo, Host host, ResponseCallback callback) {
		ByteBuffer buffer = ByteBuffer.allocate(
				NetworkRequest.REQUEST_TYPE
						+ NetworkRequest.FILENAME_LENGTH
						+ fileInfo.getFilename().getBytes().length
						+ NetworkRequest.FILE_LENGTH
						+ (int)fileInfo.getFileLength());

		buffer.putInt(NetworkRequest.REQUEST_SEND_FILE);
		buffer.putInt(fileInfo.getFilename().getBytes().length); // 先放入4个字节的int，是一个数据，527,336 代表了这里的文件名有多少个字节
		buffer.put(fileInfo.getFilename().getBytes()); // 再把真正的文件名给放入进去
		buffer.putLong(fileInfo.getFileLength()); // long对应了8个字节，放到buffer里去
		buffer.put(fileInfo.getFile());
		buffer.rewind();

		NetworkRequest request = new NetworkRequest();
		request.setId(UUID.randomUUID().toString());
		request.setHostname(host.getHostname());
		request.setIp(host.getIp());
		request.setNioPort(host.getNioPort());
		request.setBuffer(buffer);
		request.setNeedResponse(false);
		request.setRequestType(NetworkRequest.REQUEST_SEND_FILE);
		request.setCallback(callback);
		return request;
	}


	/**
	 * 发送一个文件过去
	 */
	public byte[] readFile(Host host, String filename, Boolean retry) throws Exception {
		if(!networkManager.maybeConnect(host.getHostname(), host.getNioPort())){
			if(retry){
				throw new Exception("");
			}
		}
		NetworkRequest request = createReadFileRequest(host, filename, null);
		networkManager.sendRequest(request);

		NetworkResponse response = networkManager.waitingResponse(request.getId());
		if(response.getError()){
			if(retry){
				throw new Exception();
			}
		}
		return response.getBuffer().array();

	}

	private NetworkRequest createReadFileRequest(Host host,  String filename,  ResponseCallback callback) {
		byte[] filenameBytes = filename.getBytes();
		// (int) (int) (file)
		ByteBuffer buffer = ByteBuffer.allocate(
				NetworkRequest.REQUEST_TYPE +
						NetworkRequest.FILENAME_LENGTH +
						filenameBytes.length);
		buffer.putInt(NetworkRequest.REQUEST_READ_FILE);
		buffer.putInt(filenameBytes.length); // 先放入4个字节的int，是一个数据，527,336 代表了这里的文件名有多少个字节
		buffer.put(filenameBytes); // 再把真正的文件名给放入进去
		buffer.rewind();

		NetworkRequest request = new NetworkRequest();
		request.setId(UUID.randomUUID().toString());
		request.setHostname(host.getHostname());
		request.setIp(host.getIp());
		request.setNioPort(host.getNioPort());
		request.setBuffer(buffer);
		request.setNeedResponse(true);
		request.setRequestType(NetworkRequest.REQUEST_READ_FILE);
		request.setCallback(callback);

		return request;
	}
	
}

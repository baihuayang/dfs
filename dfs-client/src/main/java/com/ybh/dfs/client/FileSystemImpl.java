package com.ybh.dfs.client;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ybh.dfs.namenode.rpc.model.*;
import com.ybh.dfs.namenode.rpc.service.NameNodeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

/**
 * 文件系统客户端的实现类
 * @author zhonghuashishan
 *
 */
public class FileSystemImpl implements FileSystem {

	private static final String NAMENODE_HOSTNAME = "localhost";
	private static final Integer NAMENODE_PORT = 50070;

	private NameNodeServiceGrpc.NameNodeServiceBlockingStub namenode;
	private NIOClient nioClient;

	public FileSystemImpl() {
		ManagedChannel channel = NettyChannelBuilder
				.forAddress(NAMENODE_HOSTNAME, NAMENODE_PORT)
				.negotiationType(NegotiationType.PLAINTEXT)
				.build();
		this.namenode = NameNodeServiceGrpc.newBlockingStub(channel);
		this.nioClient = new NIOClient();
	}

	/**
	 * 创建目录
	 */
	@Override
	public void mkdir(String path) throws Exception {
		MkdirRequest request = MkdirRequest.newBuilder()
				.setPath(path)
				.build();

		MkdirResponse response = namenode.mkdir(request);

		System.out.println("创建目录的响应：" + response.getStatus());
	}


	/**
	 * 优雅关闭
	 * @throws Exception
	 */
	@Override
	public void shutdown() throws Exception {
		ShutdownRequest request = ShutdownRequest.newBuilder()
				.setCode(1)
				.build();
		namenode.shutdown(request);
	}

	/**
	 * 上传文件
	 * @throws Exception
	 */
	@Override
	public Boolean upload(FileInfo fileInfo, ResponseCallback responseCallback) throws Exception {
		// 必须先用filename 发送一个RPC到 namenode
		// 文件目录树创建一个文件
		// 查重，存在就不上传了
		if(!createFile(fileInfo.getFilename())) {
			return false;
		}

		// /image/product/iphone.jpg
		// 希望的是每个数据节点在无力存储的时候，其实就是会在DATA_DIR下面去建立
		// D:\\dfs-test\\tmp\\image\product 目录
		// 目录里有 iphone.jpg


		// 找mster节点去要多个数据节点的地址
		// 需要上传几个副本
		// 保证每个数据节点放的数据量是比较均衡的
		JSONArray datanodes = allocateDataNodes(fileInfo.getFilename(), fileInfo.getFileLength());
		// 依次把文件副本上传到各个数据节点去
		// 考虑上传失败
		// 容错机制
		for(int i=0; i<datanodes.size(); i++){
			Host host = getHost(datanodes.getJSONObject(i));
			if(!nioClient.sendFile(fileInfo, host, responseCallback)){
				host = reallocateDataNode(fileInfo, host.getId());
				nioClient.sendFile(fileInfo, host, null);
			}
		}
		return true;
	}

	/**
	 * 重试上传文件
	 * @param fileInfo
	 * @param excludeHost
	 * @return
	 * @throws Exception
	 */
	@Override
	public Boolean retryUpload(FileInfo fileInfo, Host excludeHost) throws Exception {
		Host host = reallocateDataNode(fileInfo, excludeHost.getId());
		nioClient.sendFile(fileInfo, host, null);
		return true;
	}

//	/**
//	 * 将文件上传到指定的数据节点
//	 * @return
//	 */
//	private Boolean uploadToDataNode(FileInfo fileInfo, Host host, ResponseCallback callback) {
//		return nioClient.sendFile(fileInfo, host, callback);
//	}

	/**
	 * 获取数据节点对应的机器
	 * @param datanode
	 * @return
	 */
	private Host getHost(JSONObject datanode) {
		Host host = new Host();
		host.setHostname(datanode.getString("hostname"));
		host.setNioPort(datanode.getInteger("nioPort"));
		host.setIp(datanode.getString("ip"));
		return host;
	}



	private Boolean createFile(String filename) {
		CreateFileRequest request = CreateFileRequest.newBuilder()
				.setFilename(filename)
				.build();
		CreateFileResponse createFileResponse = namenode.create(request);

		if (createFileResponse.getStatus() == 1) {
			return true;
		}
		return true;

	}

	/**
	 * 分配双副本对应数据节点
	 * @param fileName
	 * @param fileSize
	 * @return
	 */
	private JSONArray allocateDataNodes(String fileName, long fileSize) {
		AllocateDataNodesRequest request = AllocateDataNodesRequest
				.newBuilder()
				.setFilename(fileName)
				.setFileSize(fileSize)
				.build();

		AllocateDataNodesResponse response = namenode.allocateDatanodes(request);
		return JSONArray.parseArray(response.getDatanodes());
	}

	/**
	 * 再次分配双副本对应数据节点
	 * @param excludedHostId
	 * @return
	 */
	private Host reallocateDataNode(FileInfo fileInfo, String excludedHostId) {
		ReallocateDataNodeRequest request = ReallocateDataNodeRequest.newBuilder()
				.setFilesize(fileInfo.getFileLength())
				.setExcludeDataNodeId(excludedHostId)
				.build();

		ReallocateDataNodeResponse response = namenode.reallocateDataNode(request);
		return getHost(JSONObject.parseObject(response.getDatanode()));
	}

	@Override
	public byte[] download(String filename) throws Exception {
		//1. 调用namenode接口，获取这个文件的某个副本所在的DataNode
		Host datanode = chooseDataNodeFromReplicas(filename, "");
		//2. 打开一个针对哪个DataNode的网络连接，发送文件名过去
		//3. 尝试从连接中读取对方传输过来的文件
		//4. 读取到文件之后，不需要写入本地的磁盘中，而是转换为一个字节数组返回即可
		byte[] file = null;
		try{
			file = nioClient.readFile(datanode, filename, true);
		} catch (Exception e) {
			e.printStackTrace();

			datanode = chooseDataNodeFromReplicas(filename, datanode.getId());
			try{
				file = nioClient.readFile(datanode, filename, false);
			}catch (Exception e2){
				throw e2;
			}
		}
		return file;
	}

	private Host chooseDataNodeFromReplicas(String filename, String excludeDataNodeId) {
		ChooseDataNodeFromReplicasRequest request = ChooseDataNodeFromReplicasRequest
				.newBuilder()
				.setFilename(filename)
				.setExcludeDataNodeId(excludeDataNodeId)
				.build();

		ChooseDataNodeFromReplicasResponse response = namenode.chooseDataNodeFromReplicas(request);
		return getHost(JSONObject.parseObject(response.getDatanode()));
	}

}

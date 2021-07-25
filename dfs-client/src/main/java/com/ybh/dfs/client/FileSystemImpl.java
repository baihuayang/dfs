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
	 * @param file 文件字节数组
	 * @param filename 文件名
	 * @throws Exception
	 */
	@Override
	public Boolean upload(byte[] file, String filename, long fileSize) throws Exception {
		// 必须先用filename 发送一个RPC到 namenode
		// 文件目录树创建一个文件
		// 查重，存在就不上传了
		if(!createFile(filename)) {
			return false;
		}
		System.out.println("在文件目录树中成功创建该文件......");

		// /image/product/iphone.jpg
		// 希望的是每个数据节点在无力存储的时候，其实就是会在DATA_DIR下面去建立
		// D:\\dfs-test\\tmp\\image\product 目录
		// 目录里有 iphone.jpg


		// 找mster节点去要多个数据节点的地址
		// 需要上传几个副本
		// 保证每个数据节点放的数据量是比较均衡的
		String datanodesJson = allocateDataNodes(filename, fileSize);
		System.out.println("申请分配了2个数据节点：" + datanodesJson);
		// 依次把文件副本上传到各个数据节点去
		// 考虑上传失败
		// 容错机制
		JSONArray datanodes = JSONArray.parseArray(datanodesJson);
		for(int i=0; i<datanodes.size(); i++){
			JSONObject datanode = datanodes.getJSONObject(i);
			String hostname = datanode.getString("hostname");
			int nioPort = datanode.getInteger("nioPort");
			nioClient.sendFile(hostname, nioPort, file, filename, fileSize);
		}
		return true;
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
	private String allocateDataNodes(String fileName, long fileSize) {
		AllocateDataNodesRequest request = AllocateDataNodesRequest
				.newBuilder()
				.setFilename(fileName)
				.setFileSize(fileSize)
				.build();

		AllocateDataNodesResponse response = namenode.allocateDatanodes(request);
		return response.getDatanodes();
	}

	@Override
	public byte[] download(String filename) {
		//1. 调用namenode接口，获取这个文件的某个副本所在的DataNode
		JSONObject datanode = getDataNodeForFile(filename);
		System.out.println("master分配用来下载文件的数据节点 " + datanode.toJSONString());
		//2. 打开一个针对哪个DataNode的网络连接，发送文件名过去
		//3. 尝试从连接中读取对方传输过来的文件
		//4. 读取到文件之后，不需要写入本地的磁盘中，而是转换为一个字节数组返回即可
		String hostname = datanode.getString("hostname");
		Integer nioPort = datanode.getInteger("nioPort");
		return nioClient.readFile(hostname, nioPort, filename);
	}

	private JSONObject getDataNodeForFile(String filename) {
		GetDataNodeForFileRequest request = GetDataNodeForFileRequest
				.newBuilder()
				.setFilename(filename)
				.build();
		GetDataNodeForFileResponse response = namenode.getDataNodeForFile(request);
		return JSONObject.parseObject(response.getDatanodeInfo());
	}

}

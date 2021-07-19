package com.ybh.dfs.client;


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
	
	public FileSystemImpl() {
		ManagedChannel channel = NettyChannelBuilder
				.forAddress(NAMENODE_HOSTNAME, NAMENODE_PORT)
				.negotiationType(NegotiationType.PLAINTEXT)
				.build();
		this.namenode = NameNodeServiceGrpc.newBlockingStub(channel);
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

		// 找mster节点去要多个数据节点的地址
		// 需要上传几个副本
		// 保证每个数据节点放的数据量是比较均衡的

		// 依次把文件副本上传到各个数据节点去
		// 考虑上传失败
		// 容错机制
		return true;
	}

	private Boolean createFile(String filename) {
		CreateFileRequest request = CreateFileRequest.newBuilder()
				.setFilename(filename)
				.build();
		CreateFileResponse createFileResponse = namenode.create(request);
		if( createFileResponse.getStatus() == 1) {
			return true;
		}
		return true;

	}

}

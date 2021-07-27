package com.ybh.dfs.datanode.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 客户端的一个 NIOClient
 */
public class NIOClient {
	public static final Integer SEND_FILE = 1;
	public static final Integer READ_FILE = 2;

	/**
	 * 发送一个文件过去
	 * @param hostname
	 * @param nioPort
	 */
	public byte[] readFile(String hostname, int nioPort, String filename) {
		ByteBuffer fileLengthBuffer = null;
		ByteBuffer fileBuffer = null;
		Long fileLength = null;

		byte[] file = null;
		// 建立一个短连接
		SocketChannel channel = null;
		Selector selector = null;
		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.connect(new InetSocketAddress(hostname, nioPort));
			selector = Selector.open();
			channel.register(selector, SelectionKey.OP_CONNECT);

			boolean reading = true;

			while(reading){
				selector.select();

				Iterator<SelectionKey> keysIterator = selector.selectedKeys().iterator();
				while(keysIterator.hasNext()){
					SelectionKey key = (SelectionKey) keysIterator.next();
					keysIterator.remove();
					// nioserver 允许连接
					if(key.isConnectable()){
						channel = (SocketChannel) key.channel();

						if(channel.isConnectionPending()){
							channel.finishConnect(); // 三次握手做完，tcp连接建立好
						}

						//1.一旦建立连接，直接发送一个请求过去
						//先发送这个请求要做的事情 Integer代表 4个字节
						//1: 发送文件 2:读取文件
						// 客户端发送后 立刻关注 OP_READ 事件，
						// 一旦读取完了 立刻关注 OP_WRITE ，发送 SUCCESS

						// 服务端，先读取开头4个字节，判断你要干什么
						// 1，则发送文件，用之前代码，2，读取文件，新逻辑
						// 最后双方都要断开连接
						byte[] filenameBytes = filename.getBytes();
						// (int) (int) (file)
						ByteBuffer readFileRequest = ByteBuffer.allocate(4 + 4 + filenameBytes.length);
						readFileRequest.putInt(READ_FILE);
						readFileRequest.putInt(filenameBytes.length); // 先放入4个字节的int，是一个数据，527,336 代表了这里的文件名有多少个字节
						readFileRequest.put(filenameBytes); // 再把真正的文件名给放入进去
						readFileRequest.flip();

						channel.write(readFileRequest);

						System.out.println("发送文件下载的请求过去......");

						key.interestOps(SelectionKey.OP_READ);
					}
					//接收到nioServer的响应
					else if(key.isReadable()){
						channel = (SocketChannel) key.channel();

						if(fileLength == null){
							if(fileLengthBuffer == null){
								fileLengthBuffer = ByteBuffer.allocate(8);
							}
							channel.read(fileLengthBuffer);
							if(!fileLengthBuffer.hasRemaining()) {
								fileLengthBuffer.rewind();
								fileLength = fileLengthBuffer.getLong();
								System.out.println("从服务端返回数据中解析文件大小: " + fileLength);
							}
						}

						if(fileLength != null){
							if(fileBuffer == null){
								fileBuffer = ByteBuffer.allocate(fileLength.intValue());
							}

							int hasRead = channel.read(fileBuffer);
							System.out.println("从服务端读取了" + hasRead + " bytes数据到内存中");

							if(!fileBuffer.hasRemaining()) {
								fileBuffer.rewind();
								file = fileBuffer.array();
								System.out.println("最终获取到文件大小为 " + file.length + " bytes");
								reading = false;
							}
						}
					}
				}
			}
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(channel != null){
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if(selector != null){
				try {
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return file;
	}
	
}

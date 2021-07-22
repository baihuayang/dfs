package com.ybh.dfs.client;

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

	/**
	 * 发送一个文件过去
	 * @param hostname
	 * @param nioPort
	 * @param file
	 * @param fileSize
	 */
	public static void sendFile(String hostname, int nioPort,
								byte[] file, String filename, long fileSize) {
		// 建立一个短连接
		SocketChannel channel = null;
		Selector selector = null;
		try {  
			channel = SocketChannel.open();  
			channel.configureBlocking(false);  
			channel.connect(new InetSocketAddress(hostname, nioPort));
			selector = Selector.open();  
			channel.register(selector, SelectionKey.OP_CONNECT);
			
			boolean sending = true;
			
			while(sending){    
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

							byte[] filenameBytes = filename.getBytes();
							
							ByteBuffer buffer = ByteBuffer.allocate((int)fileSize * 2 + filenameBytes.length);
							buffer.putInt(filenameBytes.length); // 先放入4个字节的int，是一个数据，527,336 代表了这里的文件名有多少个字节

							buffer.put(filenameBytes); // 再把真正的文件名给放入进去

							buffer.putLong(fileSize); // long对应了8个字节，放到buffer里去
							buffer.put(file);
							buffer.flip();

							int sendData = channel.write(buffer);
							channel.register(selector, SelectionKey.OP_READ);
						}   
					}
					//接收到nioserver的响应
					else if(key.isReadable()){  
						channel = (SocketChannel) key.channel();
						
						ByteBuffer buffer = ByteBuffer.allocate(1024);                         
						int len = channel.read(buffer); 
						
						if(len > 0) {
							System.out.println("[" + Thread.currentThread().getName() 
									+ "]收到" + hostname + "的响应：" + new String(buffer.array(), 0, len));
							sending = false;
						}
					}
				} 
			}                            
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
	}
	
}

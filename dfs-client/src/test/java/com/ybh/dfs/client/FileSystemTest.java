package com.ybh.dfs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileSystemTest {

	private static FileSystem fileSystem = new FileSystemImpl();
	
	public static void main(String[] args) throws Exception {
//		testMkdir();
//		testShutdown();
		testCreateFile();
//		testReadFile();
	}

	private static void testMkdir() {
		for(int j=0; j<10;j++){
			new Thread() {
				@Override
				public void run() {
					for(int i=0;i<100;i++){
						try {
							fileSystem.mkdir("/usr/warehouse/hadoop" + i + "_" + Thread.currentThread().getName());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
	}

	private static void testShutdown() throws Exception {
		fileSystem.shutdown();
	}

	private static void testCreateFile() throws Exception {
		File image = new File("D:\\dfs-test\\tmp\\huiyuan01.jpeg");
		long fileLength = image.length();
		System.out.println("准备上传的文件大小为 " + fileLength);

		final ByteBuffer buffer = ByteBuffer.allocate((int) fileLength);

		FileInputStream imageIn = new FileInputStream(image);
		FileChannel imageChannel = imageIn.getChannel();
		int hasRead = imageChannel.read(buffer);
		System.out.println("从磁盘文件里读取了" + hasRead + "bytes 的数据到内存中");

		buffer.flip();
		String filename = "/image/product/xiaoai.jpg";
		final byte[] file = buffer.array();

		final FileInfo fileInfo = new FileInfo();
		fileInfo.setFilename(filename);
		fileInfo.setFileLength(fileLength);
		fileInfo.setFile(file);

		fileSystem.upload(fileInfo, new ResponseCallback() {
			@Override
			public void process(NetworkResponse response) {
				if(response.getError()){
					Host excludedHost = new Host();
					excludedHost.setHostname(response.getHostname());
					excludedHost.setIp(response.getIp());
					try {
						fileSystem.retryUpload(fileInfo, excludedHost);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					ByteBuffer byteBuffer = response.getBuffer();
					String responseStatus = new String(byteBuffer.array(), 0, buffer.remaining());
					System.out.println("文件上传完毕，响应结果为: " + responseStatus);
				}
			}
		});

		imageIn.close();
		imageChannel.close();
	}

	private static void testReadFile() throws Exception {
		byte[] image = fileSystem.download("/image/product/xiaoai.jpg");
		ByteBuffer wrap = ByteBuffer.wrap(image);
		FileOutputStream imageOut = new FileOutputStream("D:\\dfs-test\\copy\\xiaoaicopy.jpg");
		FileChannel channel = imageOut.getChannel();
		channel.write(wrap);
		channel.close();
		image.clone();
	}
	
}

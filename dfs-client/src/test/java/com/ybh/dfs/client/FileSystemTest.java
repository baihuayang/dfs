package com.ybh.dfs.client;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileSystemTest {

	private static FileSystem fileSystem = new FileSystemImpl();
	
	public static void main(String[] args) throws Exception {
//		testMkdir();
		testShutdown();
//		testCreateFile();
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
		long imageLength = image.length();

		ByteBuffer buffer = ByteBuffer.allocate((int) imageLength);

		FileInputStream imageIn = new FileInputStream(image);
		FileChannel imageChannel = imageIn.getChannel();
		imageChannel.read(buffer);

		buffer.flip();
		byte[] imageBytes = buffer.array();
		System.out.println(imageBytes.length);

		fileSystem.upload(imageBytes, "/image/product/xiaoai.jpg", imageBytes.length);

		imageIn.close();
		imageChannel.close();
	}
	
}

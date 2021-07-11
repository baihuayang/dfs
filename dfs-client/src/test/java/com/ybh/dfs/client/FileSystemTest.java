package com.ybh.dfs.client;

public class FileSystemTest {
	
	public static void main(String[] args) throws Exception {
		final FileSystem filesystem = new FileSystemImpl();
		for(int j=0; j<10;j++){
			new Thread() {
				@Override
				public void run() {
					for(int i=0;i<100;i++){
						try {
							filesystem.mkdir("/usr/warehouse/hive" + i + "_" + Thread.currentThread().getName());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
//		filesystem.shutdown();
	}
	
}

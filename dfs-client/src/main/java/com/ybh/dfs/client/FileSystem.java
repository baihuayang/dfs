package com.ybh.dfs.client;

/**
 * 作为文件系统的接口
 * @author zhonghuashishan
 *
 */
public interface FileSystem {

	/**
	 * 创建目录
	 * @param path 目录对应的路径
	 * @throws Exception
	 */
	void mkdir(String path) throws Exception;

	/**
	 * 优雅关闭
	 * @throws Exception
	 */
	void shutdown() throws Exception;

	/**
	 * @throws Exception
	 */
	Boolean upload(FileInfo file, ResponseCallback responseCallback) throws Exception;

	/**
	 * 重试上传文件
	 * @param fileInfo
	 * @param excludeHost
	 * @return
	 * @throws Exception
	 */
	Boolean retryUpload(FileInfo fileInfo, Host excludeHost) throws Exception;

	/**
	 * 下载文件
	 * @param filename
	 * @return
	 */
	byte[] download(String filename) throws Exception;
}

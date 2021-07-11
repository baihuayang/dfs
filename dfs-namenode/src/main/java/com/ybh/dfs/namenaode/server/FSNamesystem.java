package com.ybh.dfs.namenaode.server;

import java.io.IOException;

/**
 * 负责管理元数据的核心组件
 * @author zhonghuashishan
 *
 */
public class FSNamesystem {

	/**
	 * 负责管理内存文件目录树的组件
	 */
	private FSDirectory directory;
	/**
	 * 负责管理edits log写入磁盘的组件
	 */
	private FSEditlog editlog;
	
	public FSNamesystem() {
		this.directory = new FSDirectory();
		this.editlog = new FSEditlog();
	}
	
	/**
	 * 创建目录
	 * @param path 目录路径
	 * @return 是否成功
	 */
	public Boolean mkdir(String path) throws Exception {
		this.directory.mkdir(path); 
		this.editlog.logEdit("{'OP':'MKDIR','PATH':'" + path + "'}");
		return true;
	}

	/**
	 * 强制内存刷入磁盘
	 */
	public void flush(){
		try {
			this.editlog.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取FSEditLog组件
	 * @return
	 */
	public FSEditlog getEditsLog() {
		return editlog;
	}

}

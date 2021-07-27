package com.ybh.dfs.namenaode.server;


import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 负责管理edits log日志的核心组件
 * @author zhonghuashishan
 *
 */
public class FSEditlog {

	private static final Long EDIT_LOG_CLEAN_INTERVAL = 30 * 1000L;

	/**
	 * 元数据管理组件
	 */
	private FSNamesystem namesystem;

	/**
	 * 当前递增到的txid的序号
	 */
	private long txidSeq = 0L;
	private String content;
	/**
	 * 内存双缓冲区
	 */
	private DoubleBuffer doubleBuffer = new DoubleBuffer();
	/**
	 * 当前是否在将内存缓冲刷入磁盘中
	 */
	private volatile Boolean isSyncRunning = false;
	/**
	 * 当前是否有线程在等待刷新下一批edits log到磁盘里去
	 */
	private volatile Boolean isWaitSync = false;
	/**
	 * 在同步到磁盘中的最大的一个txid
	 */
	private volatile Long syncTxid = 0L;

	/**
	 * 是否正在调度一次刷盘操作
	 */
	private volatile Boolean isSchedulingSync = false;
	/**
	 * 每个线程自己本地的txid副本
	 */
	private ThreadLocal<Long> localTxid = new ThreadLocal<Long>();
	
	// 就会导致说，对一个共享的map数据结构出现多线程并发的读写的问题
	// 此时对这个map的读写是不是就需要加锁了
//	private Map<Thread, Long> txidMap = new HashMap<Thread, Long>();
	public FSEditlog( FSNamesystem namesystem) {
		this.namesystem = namesystem;
		EditLogCleaner editLogCleaner = new EditLogCleaner();
		editLogCleaner.start();
	}
	/**
	 * 记录edits log日志
	 */
	public void logEdit(String content) {
		// 这里必须得直接加锁
		synchronized(this) {
			//是否有人在刷盘
			waitSchedulingSync();

			// 获取全局唯一递增的txid，代表了edits log的序号
			txidSeq++;
			long txid = txidSeq;
			localTxid.set(txid); // 放到ThreadLocal里去，相当于就是维护了一份本地线程的副本
			
			// 构造一条edits log对象
			EditLog log = new EditLog(txid, content); 
			
			// 将edits log写入内存缓冲中，不是直接刷入磁盘文件
			try {
				doubleBuffer.write(log);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(!doubleBuffer.shouldSyncToDisk()){
				return;
			}
			//代码进行到这里 需要刷磁盘
			isSchedulingSync = true;
		}
		
		logSync();
	}


	/**
	 * 等待正在调度刷磁盘操作
	 */
	private void waitSchedulingSync() {
		try{
			while(isSchedulingSync){
				wait(1000);
			}
		}catch (Exception e){

		}
	}

	/**
	 * 将内存缓冲中的数据刷入磁盘文件中
	 * 在这里尝试允许某一个线程一次性将内存缓冲中的数据刷入磁盘文件中
	 * 相当于实现一个批量将内存缓冲数据刷磁盘的过程
	 */
	private void logSync() {
		// 再次尝试加锁
		synchronized(this) {
			long txid = localTxid.get(); // 获取到本地线程的副本
			// 如果说当前正好有人在刷内存缓冲到磁盘中去
			if(isSyncRunning) {
				// 那么此时这里应该有一些逻辑判断
				
				// 假如说某个线程已经把txid = 1,2,3,4,5的edits log都从syncBuffer刷入磁盘了
				// 或者说此时正在刷入磁盘中
				// 此时syncMaxTxid = 5，代表的是正在输入磁盘的最大txid
				// 那么这个时候来一个线程，他对应的txid = 3，此时他是可以直接返回了
				// 就代表说肯定是他对应的edits log已经被别的线程在刷入磁盘了
				// 这个时候txid = 3的线程就不需要等待了
				if(txid <= syncTxid) { // todo 感觉不需要
					return;
				}

				while(isSyncRunning) {
					try {
						wait(1000);
					} catch (Exception e) {
						e.printStackTrace();  
					}
				}
				isWaitSync = false;
			}
			
			// 交换两块缓冲区
			doubleBuffer.setReadyToSync();
			// 然后可以保存一下当前要同步到磁盘中去的最大的txid
			// 此时editLogBuffer中的syncBuffer这块区域，交换完以后这里可能有多条数据
			// 而且他里面的edits log的txid一定是从小到大的
			// 此时要同步的txid = 6,7,8,9,10,11,12
			// syncMaxTxid = 12
			syncTxid = txid;
			// 设置当前正在同步到磁盘的标志位
			isSchedulingSync = false;
			notifyAll(); //唤醒卡在while 线程
			isSyncRunning = true;
		}
		
		// 开始同步内存缓冲的数据到磁盘文件里去
		// 这个过程其实是比较慢，基本上肯定是毫秒级了，弄不好就要几十毫秒
		try{
			doubleBuffer.flush();
		}catch (Exception e){
			e.printStackTrace();
		}

		synchronized(this) {
			// 同步完了磁盘之后，就会将标志位复位，再释放锁
			isSyncRunning = false;
			// 唤醒可能正在等待他同步完磁盘的线程
			notifyAll();
		}
	}
	
	/**
	 * 代表了一条edits log
	 * @author zhonghuashishan
	 *
	 */
	class EditLog {
	
		private long txid;
		private String content;
		
		public EditLog(long txid, String content) {
			this.txid = txid;
			JSONObject jsonObject = JSONObject.parseObject(content);
			jsonObject.put("txid", txid);
			this.content = jsonObject.toJSONString();
		}

		public long getTxid() {
			return txid;
		}

		public void setTxid(long txid) {
			this.txid = txid;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		@Override
		public String toString() {
			return "EditLog{" +
					"txid=" + txid +
					", content='" + content + '\'' +
					'}';
		}
	}

	/**
	 * 强制内存刷入磁盘
	 */
	public void flush() throws IOException {
		doubleBuffer.setReadyToSync();
		doubleBuffer.flush();
	}

	public List<String> getFlushedTxids() {
//		synchronized (this){
		return doubleBuffer.getFlushedTxids();
//		}
	}

	/**
	 * todo 这个方法 是否和 上面方法（getFlushedTxids） 合并为一个锁，之间有可能刷盘
	 * 获取当前缓冲区的数据
	 * @return
	 */
	public String[] getBufferedEditsLog() {
		synchronized (this){
			//这边只要获取到了锁，意味着
			//肯定没有人当前修改内存数据了
			//可以获取到内存缓冲的数据
			return doubleBuffer.getBufferedEditsLog();
		}
	}

	/**
	 * 自动清理editlog文件
	 */
	class EditLogCleaner extends Thread {
		@Override
		public void run() {
			System.out.println("editlog日志文件 后台清理启动......");
			while(true) {
				try{
					Thread.sleep(EDIT_LOG_CLEAN_INTERVAL);
					List<String> flushedTxids = getFlushedTxids();
					if(flushedTxids != null && flushedTxids.size() > 0){
						long checkpointTxid = namesystem.getCheckpointTxid();

						for(String flushedTxid : flushedTxids){
							String[] flushedTxidSplited = flushedTxid.split("_");
							long startTxid = Long.valueOf(flushedTxidSplited[0]);
							long endTxid = Long.valueOf(flushedTxidSplited[1]);
							if(checkpointTxid >= endTxid){
								// 此时删除文件
								String deletePath = "D:\\dfs-test\\namenode\\dfs_edits-" + (startTxid) + "-" + endTxid + ".log";
								File file = new File(deletePath);
								if(file.exists()){
									file.delete();
									System.out.println("发现editlog日志文件不需要，删除:" + deletePath);
								}
							}
						}
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
}

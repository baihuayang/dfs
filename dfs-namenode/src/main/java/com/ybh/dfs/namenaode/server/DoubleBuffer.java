package com.ybh.dfs.namenaode.server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存双缓冲
 * @author zhonghuashishan
 *
 */
public class DoubleBuffer {

	/**
	 * 单块 editslog 缓冲区大小
	 */
	public static final Integer EDIT_LOG_BUFFER_LIMIT = 25 * 1024;

	/**
	 * 是专门用来承载线程写入edits log
	 */
	EditLogBuffer currentBuffer = new EditLogBuffer();
	/**
	 * 专门用来将数据同步到磁盘中去的一块缓冲
	 */
	EditLogBuffer syncBuffer = new EditLogBuffer();
	/**
	 * 最大txid
	 */
	private long startTxid = 1L;

	/**
	 * 已经刷入磁盘中的txid
	 */
	private List<String> flushedTxids = new CopyOnWriteArrayList<>();

	/**
	 * 将edits log写到内存缓冲里去
	 * @param log
	 */
	public void write(FSEditlog.EditLog log) throws IOException {
		currentBuffer.write(log);
	}



	/**
	 * 交换两块缓冲区，为了同步内存数据到磁盘做准备
	 */
	public void setReadyToSync() {
		EditLogBuffer tmp = currentBuffer;
		currentBuffer = syncBuffer;
		syncBuffer = tmp;
	}



	/**
	 * 将syncBuffer缓冲区中的数据刷入磁盘中
	 */
	public void flush() throws IOException {
		syncBuffer.flush();
		syncBuffer.clear();
	}

	/**
	 * 获取已经刷入磁盘的editslog数据
	 * @return
	 */
	public List<String> getFlushedTxids(){
		return flushedTxids;
	}

	/**
	 * 获取当前缓冲区里的数据
	 * @return
	 */
	public String[] getBufferedEditsLog() {
		if(currentBuffer.size() == 0){
			return null;
		}
		String editsLogsRawData = new String(currentBuffer.getBufferData());
		return editsLogsRawData.split("\n");
	}

	/**
	 * 判断当前缓冲区 是否写满了,是的话 ，刷入磁盘
	 * @return
	 */
	public boolean shouldSyncToDisk() {
		if(currentBuffer.size() >= EDIT_LOG_BUFFER_LIMIT){
			return true;
		}
		return false;
	}

	class EditLogBuffer {
		/**
		 * 字节数组IO流
		 */
		ByteArrayOutputStream buffer;
		/**
		 * 上次最大
		 */
		long endTxid = 0L;

		public EditLogBuffer() {
			buffer = new ByteArrayOutputStream(EDIT_LOG_BUFFER_LIMIT * 2);

		}
		/**
		 * 将editslog日志写入缓冲区
		 * @param log
		 */
		public void write(FSEditlog.EditLog log) throws IOException {
			endTxid = log.getTxid();
			buffer.write(log.getContent().getBytes());
			buffer.write("\n".getBytes());
			System.out.println("写入一条editslog: "+log.getContent()
					+ ", 当前缓冲区大小是：" + size());
		}

		/**
		 * 获取当前缓冲区 已经写入字节数量
		 * @return
		 */
		public Integer size() {
			return buffer.size();
		}

		/**
		 * 将sync buffer 中的数据刷入磁盘中
		 */
		public void flush() throws IOException {
			byte[] data = buffer.toByteArray();
			ByteBuffer dataBuffer = ByteBuffer.wrap(data);

			String editsLogDirPath = "D:\\dfs-test\\namenode\\dfs_edits-"
					+ startTxid + "-" + endTxid + ".log";
			flushedTxids.add(startTxid + "_" + endTxid);

			RandomAccessFile file = null;
			FileOutputStream out = null;
			FileChannel editslogFileChannel = null;
			try{
				file = new RandomAccessFile(editsLogDirPath, "rw");
				out = new FileOutputStream(file.getFD());
				editslogFileChannel = out.getChannel();
				editslogFileChannel.write(dataBuffer);
				editslogFileChannel.force(false);
			} finally {
				if(out != null){
					out.close();
				}
				if(file != null){
					file.close();
				}
				if(editslogFileChannel != null){
					editslogFileChannel.close();
				}
			}
			startTxid = endTxid + 1;

		}

		/**
		 * 清空内存缓冲数据
		 */
		public void clear(){
			buffer.reset();
		}

		/**
		 * 获取内存缓冲区，当前数据
		 * @return
		 */
		public byte[] getBufferData() {
			return buffer.toByteArray();
		}
	}
}
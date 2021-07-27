package com.ybh.dfs.namenaode.server;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.ybh.dfs.namenaode.server.FSDirectory.INode;

/**
 * 负责管理元数据的核心组件
 * @author zhonghuashishan
 *
 */
public class FSNamesystem {
	public static final Integer REPLICATE_NUM = 2;

	/**
	 * 负责管理内存文件目录树的组件
	 */
	private FSDirectory directory;
	/**
	 * 负责管理edits log写入磁盘的组件
	 */
	private FSEditlog editlog;

	private long checkpointTxid = 0;

	/**
	 * 每个文件对应的副本所在的DataNode
	 */
	private Map<String, List<DataNodeInfo>> replicasByFilename =
			new HashMap<>();

	/**
	 * 每个DataNode对应的files
	 */
	private Map<String, List<String>> filesByDatanode =
			new HashMap<>();

	ReentrantReadWriteLock replicasLock = new ReentrantReadWriteLock();


	/**
	 * 数据节点的管理组件
	 */
	private DataNodeManager dataNodeManager;

	public FSNamesystem(DataNodeManager dataNodeManager) {
		this.directory = new FSDirectory();
		this.editlog = new FSEditlog(this);
		this.dataNodeManager = dataNodeManager;
		recoverNamespace();
	}
	
	/**
	 * 创建目录
	 * @param path 目录路径
	 * @return 是否成功
	 */
	public Boolean mkdir(String path) throws Exception {
		this.directory.mkdir(path); 
		this.editlog.logEdit(EditlogFactory.mkdir(path));
		return true;
	}

	/**
	 * 创建文件
	 * @param filename 文件名，包含所在绝对路径, /usr/warehouse
	 * @return
	 * @throws Exception
	 */
	public Boolean create(String filename) throws Exception {
		if(!directory.create(filename)) {
			return false;
		}
		this.editlog.logEdit(EditlogFactory.create(filename));
		// 写一条editlog
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

	public void setCheckpointTxid(long checkpointTxid) {
		System.out.println("接收到checkpoint txid:" + checkpointTxid);
		this.checkpointTxid = checkpointTxid;
	}

	public long getCheckpointTxid() {
		return checkpointTxid;
	}

	/**
	 * 將checkpoint txid 保存到磁盘上去
	 */
	public void saveCheckpointTxid() {
		String path = "D:\\dfs-test\\namenode\\checkpoint-txid.meta";

		RandomAccessFile raf = null;
		FileOutputStream out = null;
		FileChannel channel = null;
		try{
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
			ByteBuffer buffer = ByteBuffer.wrap(String.valueOf(checkpointTxid).getBytes());

			raf = new RandomAccessFile(path, "rw");
			out = new FileOutputStream(raf.getFD());
			channel = out.getChannel();

			channel.write(buffer);
			channel.force(false);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				if(out != null){
					out.close();
				}
				if(raf != null){
					raf.close();
				}
				if(channel != null){
					channel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 恢复元数据
	 */
	public void recoverNamespace(){
		try {
			loadFSImage();
			loadCheckpointTxid();
			loadEditLog();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 加载fsimage 文件到内存来进行恢复
	 */
	private void loadFSImage() throws Exception{
		FileInputStream in = null;
		FileChannel channel = null;
		try{
			String path = "D:\\dfs-test\\namenode\\fsimage.meta";
			File file = new File(path);
			if(!file.exists()){
				System.out.println("fsimage文件当前不存在，不进行恢复");
				return;
			}
			in = new FileInputStream(path);
			channel = in.getChannel();
			//1024 * 1024 写死先
			ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
			int count = channel.read(buffer);

			buffer.flip();
			String fsimageJson = new String(buffer.array(), 0, count);
			System.out.println("恢复fsimage文件中的数据:" + fsimageJson);

			INode dirTree = JSONObject.parseObject(fsimageJson, INode.class);
			directory.setDirTree(dirTree);
		}finally {
			if(in != null){
				in.close();
			}
			if(channel != null){
				channel.close();
			}
		}
	}

	/**
	 * 加载和回放editslog
	 * @throws Exception
	 */
	private void loadEditLog() throws Exception{

		File dir = new File("D:\\dfs-test\\namenode\\");
		List<File> files = new ArrayList<>();
		for(File file : dir.listFiles()){
			files.add(file);
		}

		Iterator<File> fileIterator = files.iterator();
		while(fileIterator.hasNext()){
			File file = fileIterator.next();
			if(!file.getName().contains("edit")){
				fileIterator.remove();
			}
		}

		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				Integer o1StartTxid = Integer.valueOf(o1.getName().split("-")[1]);
				Integer o2StartTxid = Integer.valueOf(o2.getName().split("-")[1]);
				return o1StartTxid - o2StartTxid;
			}
		});

		if(files == null || files.size() == 0){
			System.out.println("当前没有任何editlog文件，不进行恢复......");
			return;
		}

		for(File file : files){
			if(file.getName().contains("edit")){
				System.out.println("准备恢复editlog文件中的数据:" + file.getName());
				String[] splitedName = file.getName().split("-");
				Long startTxid = Long.valueOf(splitedName[1]);
				Long endTxid = Long.valueOf(splitedName[2].split("[.]")[0]);

				// 如果checkkpointTxid 之后的 editlog 都要加载出来
				if(endTxid > checkpointTxid) {
					String currentEditsLogFile = "D:\\dfs-test\\namenode\\dfs_edits-"
							+ startTxid + "-" + endTxid + ".log";
					List<String> editsLogs = Files.readAllLines(Paths.get(currentEditsLogFile),
							StandardCharsets.UTF_8);
					for(String editLogJson : editsLogs){
						JSONObject editLog = JSONObject.parseObject(editLogJson);
						long txid = editLog.getLongValue("txid");
						if(txid > checkpointTxid){
							System.out.println("准备回放editlog:" + editLogJson);
							// 回放到内存
							String op = editLog.getString("OP");
							if(op.equals("MKDIR")){
								String path = editLog.getString("PATH");
								try {
									directory.mkdir(path);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else if(op.equals("CREATE")) {
								String path = editLog.getString("PATH");
								try{
									directory.create(path);
								}catch (Exception e){
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 加载
	 * @return
	 * @throws Exception
	 */
	private void loadCheckpointTxid() throws Exception {
		FileInputStream in = null;
		FileChannel channel = null;
		try{
			String path = "D:\\dfs-test\\namenode\\checkpoint-txid.meta";
			File file = new File(path);
			if(!file.exists()){
				System.out.println("checkpoint txid 文件不存在，不进行恢复");
				return;
			}

			in = new FileInputStream(path);
			channel = in.getChannel();
			//1024 * 1024 写死先
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			int count = channel.read(buffer);

			buffer.flip();
			Long checkpointTxid = Long.valueOf(new String(buffer.array(), 0, count));
			System.out.println("恢复checkpoint txid:" + checkpointTxid);

			this.checkpointTxid = checkpointTxid;
		}finally {
			if(in != null){
				in.close();
			}
			if(channel != null){
				channel.close();
			}
		}
	}

	/**
	 * 给指定文件增加一个成功接收的副本
	 * @param filename
	 * @throws Exception
	 */
	public void addReceivedReplica(String hostname, String ip, String filename, Long fileLength) {
		try{
			replicasLock.writeLock().lock();
			DataNodeInfo datanode = dataNodeManager.getDatanode(ip, hostname);
			// 维护file 对应的datanodes
			List<DataNodeInfo> replicas = replicasByFilename.get(filename);
			if(replicas == null) {
				replicas = new ArrayList<>();
				replicasByFilename.put(filename, replicas);
			}
			if(replicas.size() == REPLICATE_NUM) {
				//减少节点存储数据量
				datanode.addStoredDataSize(-fileLength); // todo 这里减掉没错，但是节点恢复后并没有新增啊
				//生成副本复制任务
				RemoveReplicaTask removeReplicaTask = new RemoveReplicaTask(filename, datanode);
				datanode.addRemoveReplicaTask(removeReplicaTask);

				return;
			}
			replicas.add(datanode);
			// 维护datanode 对应files
			List<String> files = filesByDatanode.get(ip + "-" + hostname);
			if(files == null){
				files = new ArrayList<>();
				filesByDatanode.put(ip + "-" + hostname, files);
			}
			files.add(filename + "_" + fileLength);

			System.out.println("收到增量上报，当前的副本信息为:" + replicasByFilename + " , " + filesByDatanode);
		}finally {
			replicasLock.writeLock().unlock();
		}
	}

	public void removeDeadDatanode(DataNodeInfo dataNodeInfo) {
		try{
			replicasLock.writeLock().lock();
			List<String> files = filesByDatanode.get(dataNodeInfo.getId());
			for(String file : files) {
				List<DataNodeInfo> replicas = replicasByFilename.get(file.split("_")[0]);
				replicas.remove(dataNodeInfo);
			}
			filesByDatanode.remove(dataNodeInfo.getId());

			System.out.println("从内存数据结构中删除掉这个数据节点关联的数据," + replicasByFilename + ", " + filesByDatanode);
		} finally {
			replicasLock.writeLock().unlock();
		}
	}



	public List<String> getFilesByDatanode(String ip, String hostname) {
		try{
			replicasLock.readLock().lock();
			return filesByDatanode.get(ip + "-" + hostname);
		} finally {
			replicasLock.readLock().unlock();
		}
	}

	public DataNodeInfo getDatanodeForFile(String filename) {
		try {
			replicasLock.readLock().lock();
			List<DataNodeInfo> dataNodeInfoList = replicasByFilename.get(filename);
			int size = dataNodeInfoList.size();

			Random random = new Random();
			int index = random.nextInt(size);
			return dataNodeInfoList.get(index);
		} finally {
			replicasLock.readLock().unlock();
		}
	}

	/**
	 * 获取复制源头节点
	 * @param filename
	 * @param deadDatanode
	 * @return
	 */
	public DataNodeInfo getReplicateSource(String filename, DataNodeInfo deadDatanode) {
		DataNodeInfo replicateSource = null;
		try{
			replicasLock.readLock().lock();
			List<DataNodeInfo> dataNodeInfoList = replicasByFilename.get(filename);
			for(DataNodeInfo nodeInfo : dataNodeInfoList) {
				if(!nodeInfo.equals(deadDatanode)) {
					replicateSource = nodeInfo;
				}
			}
		}finally {
			replicasLock.readLock().unlock();
		}
		return replicateSource;
	}

}

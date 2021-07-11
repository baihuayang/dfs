package com.ybh.dfs.backupnode.server;

import com.alibaba.fastjson.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 负责管理内存中的文件目录树的核心组件
 * @author zhonghuashishan
 *
 */
public class FSDirectory {
	
	/**
	 * 内存中的文件目录树
	 */
	private INodeDirectory dirTree;

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	/**
	 * 读写锁加锁和释放锁
	 */
	public void writeLock() {
		lock.writeLock().lock();
	}
	public void writeUnLock() {
		lock.writeLock().unlock();
	}
	public void readLock() {
		lock.readLock().lock();
	}
	public void readUnLock() {
		lock.readLock().unlock();
	}

	/**
	 * 当前文件目录树 更新到了哪个txid
	 */
	private long maxTxid = 0l;

	public FSDirectory() {
		this.dirTree = new INodeDirectory("/");  
	}

	/**
	 * 以json格式获取到fsimage内存元数据
	 * @return
	 */
	public FSImage getFSImage() {
		FSImage fsImage = null;
		try{
			readLock();
			String fsimageJson = JSONObject.toJSONString(dirTree);

			// 在这个时刻，还需要知道，当前这份元数据同步到 最大 txid 是多少
			fsImage = new FSImage(maxTxid, fsimageJson);
		}finally {
			readUnLock();
		}
		return fsImage;
	}



	/**
	 * 创建目录
	 * @param path 目录路径
	 */
	public void mkdir(long txid, String path) {
		// path = /usr/warehouse/hive
		// 你应该先判断一下，“/”根目录下有没有一个“usr”目录的存在
		// 如果说有的话，那么再判断一下，“/usr”目录下，有没有一个“/warehouse”目录的存在
		// 如果说没有，那么就得先创建一个“/warehosue”对应的目录，挂在“/usr”目录下
		// 接着再对“/hive”这个目录创建一个节点挂载上去
	
		try {
			writeLock();
			maxTxid = txid;
			String[] pathes = path.split("/");
			INodeDirectory parent = dirTree;
			
			for(String splitedPath : pathes) {
				if(splitedPath.trim().equals("")) {
					continue;
				}
				// /usr/local
				INodeDirectory dir = findDirectory(parent, splitedPath);

				if(dir != null) {
					parent = dir;
					continue;
				}
				
				INodeDirectory child = new INodeDirectory(splitedPath); 
				parent.addChild(child);
				parent = child;
			}
		} finally {
			writeUnLock();
		}

//		System.out.println("打印当前目录树：" + dirTree);
//		printDirTree(dirTree, "");
	}

	private void printDirTree(INodeDirectory dirTree, String space) {
		if(dirTree.children.size() == 0){
			return;
		}
		for(INode dir : dirTree.getChildren()){
			System.out.println(space + ((INodeDirectory) dir).getPath());
			printDirTree((INodeDirectory) dir, space+" ");
		}
	}


	/**
	 * 对文件目录树递归查找目录
	 * @param dir
	 * @param path
	 * @return
	 */ // /usr/local/usr /local
	private INodeDirectory findDirectory(INodeDirectory dir, String path) {
		if(dir.getChildren().size() == 0) {
			return null;
		}
		
		for(INode child : dir.getChildren()) {
			if(child instanceof INodeDirectory) {
				INodeDirectory childDir = (INodeDirectory) child;
				
				if((childDir.getPath().equals(path))) {
					return childDir;
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * 代表的是文件目录树中的一个节点
	 * @author zhonghuashishan
	 *
	 */
	private interface INode {
		
	}
	
	/**
	 * 代表文件目录树中的一个目录
	 * @author zhonghuashishan
	 *
	 */
	public static class INodeDirectory implements INode {
		
		private String path;
		private List<INode> children;
		
		public INodeDirectory(String path) {
			this.path = path;
			this.children = new LinkedList<INode>();
		}
		
		public void addChild(INode inode) {
			this.children.add(inode);
		}
		
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public List<INode> getChildren() {
			return children;
		}
		public void setChildren(List<INode> children) {
			this.children = children;
		}

		@Override
		public String toString() {
			return "INodeDirectory{" +
					"path='" + path + '\'' +
					", children=" + children +
					'}';
		}
	}
	
	/**
	 * 代表文件目录树中的一个文件
	 * @author zhonghuashishan
	 *
	 */
	public static class INodeFile implements INode {
		
		private String name;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "INodeFile{" +
					"name='" + name + '\'' +
					'}';
		}
	}

	@Override
	public String toString() {
		return "FSDirectory{" +
				"dirTree=" + dirTree +
				'}';
	}
}

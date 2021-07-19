package com.ybh.dfs.namenaode.server;

import java.util.LinkedList;
import java.util.List;

/**
 * 负责管理内存中的文件目录树的核心组件
 * @author zhonghuashishan
 *
 */
public class FSDirectory {
	
	/**
	 * 内存中的文件目录树
	 */
	private INode dirTree;

	public FSDirectory() {
		this.dirTree = new INode("/");
	}
	
	/**
	 * 创建目录
	 * @param path 目录路径
	 */
	public void mkdir(String path) {
		// path = /usr/warehouse/hive
		// 你应该先判断一下，“/”根目录下有没有一个“usr”目录的存在
		// 如果说有的话，那么再判断一下，“/usr”目录下，有没有一个“/warehouse”目录的存在
		// 如果说没有，那么就得先创建一个“/warehosue”对应的目录，挂在“/usr”目录下
		// 接着再对“/hive”这个目录创建一个节点挂载上去
	
		synchronized(dirTree) {
			String[] pathes = path.split("/");
			INode parent = dirTree;
			
			for(String splitedPath : pathes) {
				if(splitedPath.trim().equals("")) {
					continue;
				}
				// /usr/local
				INode dir = findDirectory(parent, splitedPath);

				if(dir != null) {
					parent = dir;
					continue;
				}
				
				INode child = new INode(splitedPath);
				parent.addChild(child);
				parent = child;
			}
		}

//		System.out.println("打印当前目录树：" + dirTree);
//		printDirTree(dirTree, "");
	}

	private void printDirTree(INode dirTree, String space) {
		if(dirTree.children.size() == 0){
			return;
		}
		for(INode dir : dirTree.getChildren()){
			System.out.println(space + ((INode) dir).getPath());
			printDirTree((INode) dir, space+" ");
		}
	}

	/**
	 * 创建文件
	 * @param filename
	 * @return
	 */
	public Boolean create(String filename) {
		// /image/product/img001.jpg
		// 解析前面路径
		synchronized (dirTree){
			String[] splitedFilename = filename.split("/");
			String realFilename = splitedFilename[splitedFilename.length - 1];
			INode parent = dirTree;

			for(int i=0;i<splitedFilename.length-1;i++) {
				if(i == 0) {
					continue;
				}
				// /usr/local
				INode dir = findDirectory(parent, splitedFilename[i]);

				if(dir != null) {
					parent = dir;
					continue;
				}

				INode child = new INode(splitedFilename[i]);
				parent.addChild(child);

				parent = child;
			}
			//此时获取到文件的上一级目录
			//此时可以查找当前这个目录下面是否有对应的文件
			if(existFile(parent, realFilename)) {
				return false;
			}

			// 真正的在目录里创建一个文件出来
			INode file = new INode(realFilename);
			parent.addChild(file);
			System.out.println(dirTree);
		}
		return true;
	}

	/**
	 * 目录下是否存在文件
	 * @param dir
	 * @param filename
	 * @return
	 */
	private Boolean existFile(INode dir, String filename) {
		if(dir.getChildren() != null && dir.getChildren().size() > 0){
			for(INode child : dir.getChildren()) {
				if(child.getPath().equals(filename)){
					return true;
				}
			}
		}
		return false;
	}


	/**
	 * 对文件目录树递归查找目录
	 * @param dir
	 * @param path
	 * @return
	 */ // /usr/local/usr /local
	private INode findDirectory(INode dir, String path) {
		if(dir.getChildren().size() == 0) {
			return null;
		}
		
		for(INode child : dir.getChildren()) {
			if(child instanceof INode) {
				INode childDir = (INode) child;
				
				if((childDir.getPath().equals(path))) {
					return childDir;
				}
			}
		}
		
		return null;
	}

	public INode getDirTree() {
		return dirTree;
	}

	public void setDirTree(INode dirTree) {
		this.dirTree = dirTree;
	}

	/**
	 * 代表文件目录树中的一个目录
	 * @author zhonghuashishan
	 *
	 */
	public static class INode {
		
		private String path;
		private List<INode> children;

		public INode(){

		}
		
		public INode(String path) {
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


	@Override
	public String toString() {
		return "FSDirectory{" +
				"dirTree=" + dirTree +
				'}';
	}
}

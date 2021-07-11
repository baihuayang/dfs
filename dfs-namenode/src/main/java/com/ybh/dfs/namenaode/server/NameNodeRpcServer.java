package com.ybh.dfs.namenaode.server;

import com.ybh.dfs.namenode.rpc.service.NameNodeServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class NameNodeRpcServer {

	private static final Logger logger = Logger.getLogger(NameNodeRpcServer.class.getName());

	private static final int DEFAULT_PORT = 50070;

	private Server server = null;

	/**
	 * 负责管理元数据的核心组件
	 */
	private FSNamesystem namesystem;
	/**
	 * 负责管理集群中所有的datanode的组件
	 */
	private DataNodeManager datanodeManager;

	public NameNodeRpcServer(FSNamesystem fsNamesystem, DataNodeManager datanodeManager){
		this.namesystem = fsNamesystem;
		this.datanodeManager = datanodeManager;
	}

	public void start() throws IOException {

		server = ServerBuilder.forPort(DEFAULT_PORT).
				addService(NameNodeServiceGrpc.bindService(new NameNodeServiceImpl(namesystem, datanodeManager)))
				.build()
				.start();

		System.out.println("Server started, listening on " + DEFAULT_PORT);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				NameNodeRpcServer.this.stop();
				System.err.println("*** server shut down");
			}
		});
	}

	public void stop() {
		if (server != null) {
			server.shutdown();
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	public void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}



}
package com.ybh.dfs.backupnode.server;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.JsonArray;
import com.ybh.dfs.namenode.rpc.model.FetchEditsLogRequest;
import com.ybh.dfs.namenode.rpc.model.FetchEditsLogResponse;
import com.ybh.dfs.namenode.rpc.model.UpdateCheckpointTxidRequest;
import com.ybh.dfs.namenode.rpc.service.NameNodeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;

public class NameNodeRpcClient {
    private static final String NAMENODE_HOSTNAME = "localhost";
    private static final Integer NAMENODE_PORT = 50070;

    private NameNodeServiceGrpc.NameNodeServiceBlockingStub namenode;
    private Boolean isNammenodeRunning = true;

    public NameNodeRpcClient() {
        ManagedChannel channel = NettyChannelBuilder
                .forAddress(NAMENODE_HOSTNAME, NAMENODE_PORT)
                .negotiationType(NegotiationType.PLAINTEXT)
                .build();
        this.namenode = NameNodeServiceGrpc.newBlockingStub(channel);
    }

    /**
     * 抓取editslog 数据
     * @return
     */
    public JSONArray fetchEditsLog(long syncedTxid) {
        FetchEditsLogRequest request = FetchEditsLogRequest.newBuilder()
                .setSyncedTxid(syncedTxid)
                .build();

        FetchEditsLogResponse response = namenode.fetchEditsLog(request);
        String editsLogJson = response.getEditsLog();

        return JSONArray.parseArray(editsLogJson);
    }

    public void updateCheckpointTxid(long txid){
        UpdateCheckpointTxidRequest request = UpdateCheckpointTxidRequest.newBuilder()
                .setTxid(txid)
                .build();

        namenode.updateCheckpointTxid(request);
    }

    public Boolean getNammenodeRunning() {
        return isNammenodeRunning;
    }

    public void setNammenodeRunning(Boolean nammenodeRunning) {
        isNammenodeRunning = nammenodeRunning;
    }
}

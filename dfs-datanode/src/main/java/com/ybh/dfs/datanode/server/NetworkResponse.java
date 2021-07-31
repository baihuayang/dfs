package com.ybh.dfs.datanode.server;

import java.nio.ByteBuffer;

public class NetworkResponse {
    private String client;
    private ByteBuffer byteBuffer;

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public void setByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}

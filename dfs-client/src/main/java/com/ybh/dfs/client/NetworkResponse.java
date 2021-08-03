package com.ybh.dfs.client;

import java.nio.ByteBuffer;

public class NetworkResponse {
    public static final String RESPONSE_SUCCESS = "SUCCESS";

    private String hostname;
    private String ip;
    private String requestId;
    private ByteBuffer lengthBuffer;
    private ByteBuffer buffer;
    private Boolean error;
    private Boolean finished;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ByteBuffer getLengthBuffer() {
        return lengthBuffer;
    }

    public void setLengthBuffer(ByteBuffer lengthBuffer) {
        this.lengthBuffer = lengthBuffer;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }
}

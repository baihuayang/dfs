package com.ybh.dfs.client;

import java.nio.ByteBuffer;

public class NetworkRequest {
    public static final Integer REQUEST_TYPE = 4;
    public static final Integer FILENAME_LENGTH = 4;
    public static final Integer FILE_LENGTH = 8;
    public static final Integer REQUEST_SEND_FILE = 1;
    public static final Integer REQUEST_READ_FILE = 2;

    private Integer requestType;
    private String id;
    private String hostname;
    private String ip;
    private Integer nioPort;
    private ByteBuffer buffer;
    private Boolean needResponse;
    private Long sendTime;
    private ResponseCallback callback;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Integer getNioPort() {
        return nioPort;
    }

    public void setNioPort(Integer nioPort) {
        this.nioPort = nioPort;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getNeedResponse() {
        return needResponse;
    }

    public void setNeedResponse(Boolean needResponse) {
        this.needResponse = needResponse;
    }

    public Integer getRequestType() {
        return requestType;
    }

    public void setRequestType(Integer requestType) {
        this.requestType = requestType;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public ResponseCallback getCallback() {
        return callback;
    }

    public void setCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

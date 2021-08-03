package com.ybh.dfs.datanode.server;

import sun.nio.ch.Net;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 请求队列包装类
 */
public class NetworkRequestQueue {   //todo 可以使用 阻塞队列吗

    private NetworkRequestQueue() {

    }

    private static volatile NetworkRequestQueue instance = null;

    public static NetworkRequestQueue get() {
        if(instance == null) {
            synchronized (NetworkRequestQueue.class) {
                if(instance == null) {
                    instance = new NetworkRequestQueue();
                }
            }
        }
        return instance;
    }

    private ConcurrentLinkedQueue<NetworkRequest> requestQueue = new ConcurrentLinkedQueue<>();

    public void offer(NetworkRequest request) {
        requestQueue.offer(request);
    }

    public NetworkRequest poll() {
        return requestQueue.poll();
    }
}

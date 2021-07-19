package com.ybh.dfs.namenaode.server;

/**
 * edit生成工程
 */
public class EditlogFactory {
    public static String mkdir(String path) {
        return "{'OP':'MKDIR','PATH':'" + path + "'}";
    }

    public static String create(String filename) {
        return"{'OP':'CREATE','PATH':'" + filename + "'}";
    }
}

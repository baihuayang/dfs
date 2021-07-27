package com.ybh.dfs.datanode.server;

import java.io.File;

import static com.ybh.dfs.datanode.server.DataNodeConfig.DATA_DIR;

public class FileUtils {
    public static String getAbsoluteFilename(String relativeFilename) {
        String[] relativeFilenameSplited = relativeFilename.split("/");
        String dirPath = DATA_DIR;
        for(int i=0; i<relativeFilenameSplited.length-1; i++){
            if( i == 0){
                continue;
            }
            dirPath += "\\" + relativeFilenameSplited[i];
        }
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        return dirPath + "\\" + relativeFilenameSplited[relativeFilenameSplited.length-1];
    }
}

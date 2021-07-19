package com.ybh.dfs.backupnode.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 负责上传fsimage到NameNode线程
 */
public class FSImageUploader extends Thread {
    private FSImage fsImage;

    public FSImageUploader(FSImage fsImage){
        this.fsImage = fsImage;
    }

    @Override
    public void run() {
        SocketChannel channel = null;
        Selector selector = null;
        try{
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("localhost", 9000));

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);

            boolean uploading = true;

            while(uploading) {
                selector.select();

                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while(keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if(key.isConnectable()){
                        channel = (SocketChannel) key.channel();
                        if(channel.isConnectionPending()){
                            channel.finishConnect();
                            ByteBuffer buffer = ByteBuffer.wrap(fsImage.getFsimageJson().getBytes());
                            channel.write(buffer);
                        }
                        channel.register(selector, SelectionKey.OP_READ);
                    }
                    else if(key.isReadable()){
                        channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                        int count = channel.read(buffer);

                        if(count > 0){
                            System.out.println("上传fsimage文件成功, 响应消息未：" +
                                    new String(buffer.array(), 0, count));
                            channel.close();
                            uploading = false;
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
          if(channel != null){
              try{
                  channel.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          if(selector != null){
              try {
                  selector.close();
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
        }
    }
}

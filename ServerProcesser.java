package com.findqu.programlearn.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerProcesser implements Runnable {
    private Logger logger = LoggerFactory.getLogger(ServerAccepter.class);


    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);//1K
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024);//1K
    private ArrayBlockingQueue<SocketChannel> queue;


    private ServerEndPoint serverEndPoint;
    private Selector selector;

    public ServerProcesser(ServerEndPoint serverEndPoint) {
        this.serverEndPoint = serverEndPoint;
    }


    public ServerProcesser(ServerEndPoint serverEndPoint, Queue queue) {
        this(serverEndPoint);
        this.queue = (ArrayBlockingQueue<SocketChannel>) queue;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        while (serverEndPoint.isRunning()) {
            SocketChannel channel = queue.poll();
            while (channel != null) {
                try {
                    channel.register(selector, SelectionKey.OP_READ);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
                channel = queue.poll();
            }
            try {
                int waitTasks = selector.select();
                //建议阻塞模式，select()  accepter线程Awake()
                if (waitTasks > 0) {
                    Set<SelectionKey> selectKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        if (socketChannel != null) {
                            if (key.isReadable()) {
                                readByteBuffer.clear();
                                int numRead = socketChannel.read(readByteBuffer);
                                while (numRead != 0) {
                                    numRead = socketChannel.read(readByteBuffer);
                                }
                                readByteBuffer.flip();
                                System.out.println("Server received:" + new String(readByteBuffer.array()));
                                key.interestOps(SelectionKey.OP_WRITE);
                            } else if (key.isWritable()) {//模拟http请求
                                writeByteBuffer.clear();
                                writeByteBuffer.put(serverEndPoint.getReplyContent().getBytes(StandardCharsets.UTF_8));
                                socketChannel.write(writeByteBuffer);
                                key.interestOps(SelectionKey.OP_READ);
                                closeScoket(key);
                                System.out.println("Server send:" + new String(writeByteBuffer.array()));
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeScoket(SelectionKey key) {
        if (key.isValid()) {
            try {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                if (socketChannel != null) {
//                    socketChannel.socket().close();
                    socketChannel.close();
                }
                key.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}




package com.findqu.programlearn.nio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerAccepter implements Runnable {

    private ArrayBlockingQueue<SocketChannel> queue;
    private ServerSocketChannel serverSocketChannel;
    private ServerEndPoint serverEndPoint;
    private IQueneDataModifyListener listener;

    public ServerAccepter(ServerEndPoint serverEndPoint) {
        this.serverEndPoint = serverEndPoint;
    }

    public ServerAccepter(Queue<SocketChannel> queue, ServerEndPoint serverEndPoint, IQueneDataModifyListener listener) {
        this.queue = (ArrayBlockingQueue<SocketChannel>) queue;
        this.serverEndPoint = serverEndPoint;
        this.listener = listener;
    }

    @Override
    public void run() {

        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.bind(new InetSocketAddress(this.serverEndPoint.getPort()));
            serverSocketChannel.configureBlocking(true);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //接受请求，Selector处理
        while (serverEndPoint.isRunning()) {

            SocketChannel socketChannel = null;
            try {
                socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                queue.add(socketChannel);
                if (listener != null) listener.add();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

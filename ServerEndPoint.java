package com.findqu.programlearn.nio;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerEndPoint {

    private IQueneDataModifyListener listener;
    private ServerProcesser processer;
    private ServerAccepter accepter;
    protected int threadPriority = Thread.NORM_PRIORITY;
    int port;
    /**
     * what reply to client content
     */
    String replyContent;
    /**
     * this endpoint is running
     */
    private boolean running;

    private Queue<SocketChannel> queue;


    public void init() {
        queue = new ArrayBlockingQueue<>(1024);
        listener = (() -> {
            if (processer != null) processer.getSelector().wakeup();
        });
        //start accept thread
        startAcceptThread(queue);
        //start processer thread
        startProcesserThread(queue);
    }

    private void startProcesserThread(Queue queue) {
        processer = new ServerProcesser(this, queue);
        Thread thread = new Thread(processer);
        thread.setName("mock-processer-exec-" + 1);
//        thread.setDaemon(true);//survivor form main thread
        thread.setPriority(threadPriority);
        thread.start();
    }

    private void startAcceptThread(Queue queue) {
        accepter = new ServerAccepter(queue, this, listener);
        Thread thread = new Thread(accepter);
        thread.setName("mock-accepter-exec-" + 1);
//        thread.setDaemon(true);
        thread.setPriority(threadPriority);
        thread.start();
    }

    public void setListener(IQueneDataModifyListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }


    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

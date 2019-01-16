package com.findqu.programlearn.nio;


public class Client {
    public static void main(String[] args) {
        ServerEndPoint endPoint = new ServerEndPoint();
        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: 38\r\n" +
                "Content-Type: text/html\r\n" +
                "sbox-size: 123456\r\n" +
                "\r\n" +
                "<html><body>Hello World!</body></html>";
        endPoint.setReplyContent(httpResponse);
        endPoint.setRunning(true);
        endPoint.setPort(8888);
        endPoint.init();
    }
}

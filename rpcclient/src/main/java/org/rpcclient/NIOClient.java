package org.rpcclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOClient {
	private SocketChannel channel;
    private Selector selector;

	public NIOClient() throws IOException {
		channel=SocketChannel.open();
		selector=Selector.open();
	}
	public void connect(String host,int port) throws IOException{
		channel.connect(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_CONNECT);
	}
	
	public void start() throws IOException{
		System.out.println("客户端启动");
        while(true){
            selector.select();
            Iterator<SelectionKey> ite = selector.selectedKeys().iterator();
            while(ite.hasNext()){
                SelectionKey key = ite.next();
                ite.remove();
                if(key.isConnectable()){
                    SocketChannel channel=(SocketChannel)key.channel();
                    
                    if(channel.isConnectionPending()){
                        channel.finishConnect();
                    }
                    
                    channel.configureBlocking(false);
                    channel.write(ByteBuffer.wrap(new String("send message to server.").getBytes()));
                    
                    channel.register(selector, SelectionKey.OP_READ);
                    System.out.println("客户端连接成功");
                }else if(key.isReadable()){ //有可读数据事件。
                    SocketChannel channel = (SocketChannel)key.channel();
                    
                    ByteBuffer buffer = ByteBuffer.allocate(10);
                    channel.read(buffer);
                    byte[] data = buffer.array();
                    String message = new String(data);
                    
                    System.out.println("recevie message from server:, size:" + buffer.position() + " msg: " + message);
                }
            }
        }
	}
	public static void main(String[] args) throws IOException {
		NIOClient client=new NIOClient();
		client.connect("localhost", 5055);
		client.start();
	}
}

package org.rpcserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOServer {
	private ServerSocketChannel serverSocketChannel;
	private Selector selector;
	private volatile boolean stop=false;
	public NIOServer(int port) throws IOException {
		init(port);
	}
	
	private void init(int port) throws IOException{
		serverSocketChannel=ServerSocketChannel.open();
		serverSocketChannel.bind(new InetSocketAddress(port));
		selector=Selector.open();
		serverSocketChannel.configureBlocking(false);

		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

	}
	
	public void stop(){
		
	}
	public void start() throws IOException{
		while(!stop){
			selector.select();
			Set<SelectionKey> selectkeys=selector.selectedKeys();
			Iterator<SelectionKey>  it=selectkeys.iterator();
			while(it.hasNext()){
				SelectionKey key=it.next();
				it.remove();
				if(!key.isValid())
					continue;
				if(key.isAcceptable()){
					handelConnect(key);
				}
				if(key.isReadable()){
					handelRead(key);
				}
				if(key.isWritable()){
					handelWrite(key);
				}
			}
			
			
		}
		
	}

	private void handelWrite(SelectionKey key) throws IOException {
		SocketChannel clinet=(SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(10);
        clinet.write(buffer.put("get data from server".getBytes()));
		System.out.println("write data:"+new String(buffer.array()));
	}

	private void handelRead(SelectionKey key) throws IOException {
		SocketChannel clinet=(SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(10);
		clinet.read(buffer);
		System.out.println("read data:"+new String(buffer.array()));
		clinet.register(selector, SelectionKey.OP_WRITE);

	}

	private void handelConnect(SelectionKey key) throws IOException {
		ServerSocketChannel serverchannel=(ServerSocketChannel) key.channel();
		SocketChannel clinet=serverchannel.accept();
		clinet.configureBlocking(false);
		ByteBuffer word=ByteBuffer.wrap("create connect ".getBytes());
		clinet.write(word);
		clinet.register(selector, SelectionKey.OP_READ);
		System.out.println("建立客户端连接");
	}
	public static void main(String args[]) throws IOException{
		NIOServer server=new NIOServer(5055);
		server.start();
	}
}

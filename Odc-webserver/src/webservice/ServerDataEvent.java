package webservice;


import java.nio.channels.SocketChannel;

public class ServerDataEvent<T> {
	public WebService server;
	public SocketChannel socket;
	public byte[] data;
	
	public ServerDataEvent(WebService server, SocketChannel socket, byte[] data) {
		this.server = server;
		this.socket = socket;
		this.data = data;
	}
}
package network;

import java.nio.channels.SocketChannel;

public class NetworkDataEvent {
	public Network server;
	public SocketChannel socket;
	public byte[] data;
	
	public NetworkDataEvent(Network server, SocketChannel socket, byte[] data) {
		this.server = server;
		this.socket = socket;
		this.data = data;
	}
}
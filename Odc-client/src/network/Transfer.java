package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import main.Mediator;
import main.TransferInfo;

import org.apache.log4j.Logger;

import utils.BaseClient;
import utils.ChangeRequest;

public class Transfer extends BaseClient {
	// Logger for this class 
	static Logger logger = Logger.getLogger(Transfer.class);

	// This thread's socket
	private SocketChannel socket;
	
	// The Mediator of the application
	Mediator mediator;

	// Maps a SocketChannel to a RspHandler
	private RspHandler rspHandler;

	// Maps a SocketChannel to a list of ByteBuffer instances
	private ByteBuffer pendingData = null;

	public Transfer(Mediator mediator, InetAddress hostAddress, int port) {
		this.mediator = mediator;
		this.hostAddress = hostAddress;
		this.port = port;
	}

	public void initTransfer() throws IOException {
		this.selector = this.initSelector();
		this.socket = this.initiateConnection();
	}

	@Override
	protected Selector initSelector() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	public boolean newIncomingTransfer(TransferInfo ti) {
		String message = "REQ " + mediator.getUsername() + " " + ti.path + ti.filename;
		byte[] data = message.getBytes();

		this.rspHandler = new RspHandler(this, mediator, ti);
		// And queue the data we want written
		this.pendingData = ByteBuffer.wrap(data);
		
		logger.debug("Data added to be written to socket.");

		// Finally, wake up our selecting thread so it can make the required changes
		// this.selector.wakeup();
		return true;
	}
	
	@Override
	protected SocketChannel initiateConnection() throws IOException {
		// Create a non-blocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
	
		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));
		logger.debug("Transfer intiated connection to " + this.hostAddress + ", " + this.port);

		// Queue a channel registration since the caller is not the 
		// selecting thread. As part of the registration we'll register
		// an interest in connection events. These are raised when a channel
		// is ready to complete connection establishment.
		logger.debug("Connecting...");
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		logger.debug("Connected!");
		return socketChannel;
	}

	/* Method used for client to nicely display error message to user. */
	@Override
	protected void displayError(String msg) {
		mediator.notifyNetworkError(msg);
	}

	@Override
	protected void handleResponse(SocketChannel socketChannel, byte[] data, int numRead) throws IOException {
		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);
		
		// And pass the response to it
		if (!rspHandler.handleResponse(rspData)) {
			// The handler has seen enough, close the connection
			socketChannel.close();
			socketChannel.keyFor(this.selector).cancel();
			// Current thread should exit.
			running = false;
		}
	}

	@Override
	protected void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			socketChannel.write(this.pendingData);
		} catch (IOException e) {
			key.cancel();
			socketChannel.close();
			mediator.notifyNetworkError("Unable to write to user! Connection canceled.");
			return;
		}
		this.pendingData = null;
		
		// We wrote away all data, so we're no longer interested
		// in writing on this socket. Switch back to waiting for
		// data.
		key.interestOps(SelectionKey.OP_READ);
		logger.debug("Data written to socket; key op is now data read.");
	}
	
	public void send(byte[] data) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			// And queue the data we want written
			this.pendingData = ByteBuffer.wrap(data);
		}
		
		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}
}

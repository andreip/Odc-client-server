package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import main.Mediator;
import main.TransferInfo;

import org.apache.log4j.Logger;

public class Transfer implements Runnable {
	// Logger for this class 
	static Logger logger = Logger.getLogger(Transfer.class);
	
	// The host:port combination to connect to
	private InetAddress hostAddress;
	private int port;

	// The selector we'll be monitoring
	private Selector selector;
	
	// This thread's socket
	private SocketChannel socket;
	
	// The Mediator of the application
	Mediator mediator;
	
	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	private TransferInfo ti;

	// A list of ChangeRequest instances
	private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a RspHandler
	private RspHandler rspHandler;

	// Maps a SocketChannel to a list of ByteBuffer instances
	private ByteBuffer pendingData = null;

	public Transfer(Mediator mediator, InetAddress hostAddress, int port) throws IOException {
		this.mediator = mediator;
		this.hostAddress = hostAddress;
		this.port = port;
	}

	public void initTransfer() throws IOException {
		this.selector = this.initSelector();
		this.socket = this.initiateConnection();
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}

	public boolean newIncomingTransfer(TransferInfo ti) {
		this.ti = ti;

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
	
	private SocketChannel initiateConnection() throws IOException {
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

	@Override
	public void run() {
		while (true) {
			try {
				synchronized(this.changeRequests) {
					Iterator<ChangeRequest> changes = this.changeRequests.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch(change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.changeRequests.clear(); 
				}
				logger.debug("Waiting for a new selection.");

				// Wait for an event on one of the registered channels
				this.selector.select();
				
				logger.debug("Selector selected something.");

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.finishConnection(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				mediator.notifyNetworkError("Connection closed by other user! Communication canceled.");
				logger.error("Connection closed by other user. Error was: " + e.toString());
			}
		}
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			mediator.notifyNetworkError("Unable to read from user! Connection canceled.");
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			mediator.notifyNetworkError("Unable to read from user! Connection canceled.");
			return;
		}

		// Handle the response
		this.handleResponse(socketChannel, this.readBuffer.array(), numRead);
	}

	private void handleResponse(SocketChannel socketChannel, byte[] data, int numRead) throws IOException {
		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);
		
		// And pass the response to it
		if (!rspHandler.handleResponse(rspData)) {
			// The handler has seen enough, close the connection
			socketChannel.close();
			socketChannel.keyFor(this.selector).cancel();
		}
	}

	private void write(SelectionKey key) throws IOException {
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
		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			// And queue the data we want written
			this.pendingData = ByteBuffer.wrap(data);
		}
		
		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}

	private void finishConnection(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
	
		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}
		
		logger.debug("Connection successful.");
	
		// Register an interest in writing on this channel
		key.interestOps(SelectionKey.OP_WRITE);
	}
}

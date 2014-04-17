package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.Mediator;
import main.TransferInfo;

import org.apache.log4j.Logger;

public class Network implements Runnable {
	// Logger for this class 
	static Logger logger = Logger.getLogger(Network.class);
	
	// The host:port combination to listen on
	private InetAddress hostAddress;
	private int port;
	
	// The thread who is going to deal with message interpretation
	private NetworkWorker worker;

	// The channel on which we'll accept connections
	private ServerSocketChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;
	
	// A list of ChangeRequest instances
	private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	
	public Mediator mediator;

	public Network(Mediator mediator, NetworkWorker worker, InetAddress hostAddress, int port) throws IOException {
		this.mediator = mediator;
		this.worker = worker;
		this.hostAddress = hostAddress;
		this.port = port;
		logger.trace("Network object created.");
	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public void initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		logger.trace("Selector and server channel initialized successfully.");
		this.selector = socketSelector;
	}
	
	public void startTransfer(TransferInfo ti, Transfer t) {
		try {
			t.initTransfer();
			t.newIncomingTransfer(ti);
			new Thread(t).start();
		} catch (IOException e) {
			mediator.notifyNetworkError("Unable to connect to user! Download canceled.");
			logger.error("Unable to connect to user. Error was: " + e.toString());
		}
	}

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(key.selector(), SelectionKey.OP_READ);
		
		logger.debug("Connection accepted from " + socketChannel.socket().getRemoteSocketAddress());
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
			return;
		}

		// Hand the data off to our worker thread
		this.worker.processData(this, socketChannel, this.readBuffer.array(), numRead);
		logger.debug("Worker proccessed data.");
	}
	 
	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		logger.debug("Writing data to channel.");
		synchronized (this.pendingData) {
			List<ByteBuffer> queue = (List<ByteBuffer>) this.pendingData.get(socketChannel);
			
			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				try {
					socketChannel.write(buf);
				} catch (IOException e) {
					key.cancel();
					socketChannel.close();
					mediator.notifyNetworkError("Unable to write to user! Connection canceled.");
					return;
				}
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}
			
			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}
	 
	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.changeRequests) {
			// Indicate we want the interest ops set changed
			this.changeRequests.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			// And queue the data we want written
			synchronized (this.pendingData) {
				List<ByteBuffer> queue = (List<ByteBuffer>) this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}
		
		// Finally, wake up our selecting thread so it can make the required changes
		this.selector.wakeup();
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Process any pending changes
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
				
				logger.debug("After pending changes processing.");

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
					if (key.isAcceptable()) {
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (IOException e) {
				mediator.notifyNetworkError("Connection closed by other user! Communication canceled.");
				logger.error("Connection closed by other user. Error was: " + e.toString());
			}
		}
	}
}

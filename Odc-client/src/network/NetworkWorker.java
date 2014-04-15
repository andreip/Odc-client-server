package network;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import main.Mediator;
import main.TransferInfo;

import org.apache.log4j.Logger;

public class NetworkWorker implements Runnable {
	// Logger for this class 
	static Logger logger = Logger.getLogger(NetworkWorker.class);
	Mediator mediator;

	public List<NetworkDataEvent> queue = new LinkedList<NetworkDataEvent>();
	// TODO: how should we use this?
	private Map<SocketChannel, TransferInfo> transfers = new HashMap<SocketChannel, TransferInfo>();
	
	public NetworkWorker(Mediator mediator) {
		this.mediator = mediator;
	}
	
	public void processData(Network server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized(queue) {
			queue.add(new NetworkDataEvent(server, socket, dataCopy));
			queue.notify();
		}
	}
	
	protected void queueFileForSending(NetworkDataEvent dataEvent, TransferInfo ti) {
		RandomAccessFile raf = null;
		FileChannel fc = null;
		
		try {
			raf = new RandomAccessFile(mediator.homeDir + ti.path + ti.filename, "r");
			fc = raf.getChannel();

			try {
				while (fc.position() < ti.filesize) {
					int size = (int)((ti.filesize - fc.position() < 4048) ? (ti.filesize - fc.position()) : 4048);
					ByteBuffer buffer = ByteBuffer.allocate(size);
					buffer.clear();
					int count = fc.read(buffer);
					buffer.flip();
					logger.debug("Read file contents in buffer: <" + new String(buffer.array()) + ">");

					if (count <= 0) {
						break;
					}
					dataEvent.server.send(dataEvent.socket, buffer.array());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			logger.error("Unable to finish queueing the file for sending. Error was: " + e.toString());
		} finally {
			try {
				if (fc != null)
					fc.close();
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				logger.error("Unable to close file/channel. Error was: " + e.toString());
			}
		}
	}

	public void run() {
		NetworkDataEvent dataEvent;

		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
				dataEvent = (NetworkDataEvent) queue.remove(0);
				String message = new String(dataEvent.data);
				String[] contents = message.split(" ");
				if (contents[0].equals("REQ")) {
					TransferInfo ti = dataEvent.server.mediator.newOutgoingTransfer(contents[1], contents[2]);
					if (ti == null) {
						logger.warn("Unable to create a new outgoing transfer. Decline request.");
						dataEvent.server.send(dataEvent.socket, "NACK".getBytes());
						continue;
					}
					ti.filesize = (int) new File(mediator.homeDir + ti.path + ti.filename).length();
					transfers.put(dataEvent.socket, ti);
					// Return positive response to sender - we accept sending the file
					String outMessage = "ACK " + ti.filesize;
					dataEvent.server.send(dataEvent.socket, outMessage.getBytes());
					// Map file in memory and send it.
					queueFileForSending(dataEvent, ti);
				} else {
					logger.warn("Buffer doesn't start with know command. Ignore.");
				}
			}
		}
	}
}
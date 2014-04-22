package webservice;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

public class WebServiceWorker implements Runnable {
	private List<ServerDataEvent> queue = new LinkedList<>();
	
	public void processData(WebService server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized(queue) {
			queue.add(new ServerDataEvent(server, socket, dataCopy));
			queue.notify();
		}
	}
	
	public void run() {
		ServerDataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
				dataEvent = (ServerDataEvent) queue.remove(0);
			}

			// Return to sender
			dataEvent.server.send(dataEvent.socket, getBytesToSendBack(dataEvent));
		}
	}

	/* Based on the request message from client, respond in a particular way.
	 *
	 * Received requests can be:
	 * - USERS ADD NAME HOST PORT
	 *   -> request the webserver to add a user (with name NAME) to the DB.
	 * - USERS REQ
	 *   -> request the webserver a complete list of all users registered
	 * - USERS EXIT NAME
	 *   -> request the webserver to delete a user (with name NAME), as he
	 *      is going to exit
	 */
	private byte[] getBytesToSendBack(ServerDataEvent dataEvent) {
		byte[] response = null;

		String[] strs = new String(dataEvent.data).split(" ");
		if (strs[0].equals("USERS")) {
			if (strs[1].equals("ADD")) {
				assert(strs.length == 5);
				dataEvent.server.addUser(strs[2], strs[3], Integer.parseInt(strs[4]));
				response = "ACK".getBytes();
			} else if (strs[1].equals("REQ")) {
				response = dataEvent.server.getUsers().getBytes();
			} else if (strs[1].equals("EXIT")) {
				assert(strs.length == 3);
				dataEvent.server.removeUser(strs[2]);
				response = "ACK".getBytes();
			}
		}
		return response;
	}
}

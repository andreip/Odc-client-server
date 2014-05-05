package webservice;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import utils.SerializationHelper;

public class WebServiceWorker implements Runnable {
	static Logger logger = Logger.getLogger(WebServiceWorker.class);

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
	 * - USER details related
	 *   - USERS ADD NAME HOST PORT
	 *     -> request the webserver to add a user (with name NAME) to the DB.
	 *   - USERS REQ
	 *     -> request the webserver a complete list of all users registered
	 * - FILE details related (for each user)
	 *   - <serialized_root> (try and deserialize it and see if it holds)
	 *     -> register given serialized TreeNode in the system.
	 *   - FILES REQ NAME
	 *     -> request TreeNode for NAME.
	 *
	 *   - USERS EXIT NAME
	 *     -> request the webserver to delete a user (with name NAME), as he
	 *        is going to exit
	 *     -> also webserver will delete the TreeNode associated w/ NAME.
	 */
	private byte[] getBytesToSendBack(ServerDataEvent dataEvent) {
		/* Default response. */
		byte[] response = "ACK".getBytes();

		/* First try and see if it's a TreeNode sent. */
		TreeNode root = null;
		try {
			root = (TreeNode)SerializationHelper.deserialize(dataEvent.data);
		} catch (ClassNotFoundException | IOException e) {}

		/* If we've got a root, then we received a TreeNode register request. */
		if (root != null) {
			String userName = root.toString();
			logger.debug("Request received: TreeNode upload for " + userName);
			/* Adding as byte[] array is easier, as it doesn't require
			 * serialization again for requests, just send it.
			 */
			dataEvent.server.addUserFiles(userName, dataEvent.data);
		} else {
			String request = new String(dataEvent.data);
			logger.debug("Request received: " + request);

			String[] strs = request.split(" ");
			if (strs[0].equals("USERS")) {
				if (strs[1].equals("ADD")) {
					assert(strs.length == 5);
					dataEvent.server.addUser(strs[2], strs[3], Integer.parseInt(strs[4]));
				} else if (strs[1].equals("REQ")) {
					response = dataEvent.server.getUsers().getBytes();
				} else if (strs[1].equals("EXIT")) {
					assert(strs.length == 3);
					dataEvent.server.removeUser(strs[2]);
					dataEvent.server.removeUserFiles(strs[2]);
				}
				logger.debug("Users are: " + dataEvent.server.getUsers());
			} else if(strs[0].equals("FILES")) {
				if (strs[1].equals("REQ")) {
					String userName = strs[2];
					System.out.println("Server received FILES REQ for " + userName);
					response = dataEvent.server.getUserFiles(userName);
				}
			}
		}
		return response;
	}
}

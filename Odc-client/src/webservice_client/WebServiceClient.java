package webservice_client;

import gui.UIMediator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import utils.BaseClient;
import utils.BaseRspHandler;
import utils.SerializationHelper;

import main.Mediator;

import static java.util.concurrent.TimeUnit.*;

public class WebServiceClient {
	static Logger logger = Logger.getLogger(WebServiceClient.class);

	private final static String WEBSERVICE_FILENAME = "webservice.properties";

	// In milliseconds;
	private static final int POLLING_INTERVAL = 1000;
	private static final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);

	private static BaseClient client = null;
	private static ScheduledFuture<?> periodicTaskHandle = null;

	/* Method which does polling for users list (from webservice)
	 * as a periodic task (at POLLING_INTERVAL ms), which never ends.
	 */
	public static void startPollingForUsers(final Mediator mediator) throws FileNotFoundException, IOException {
		final BaseClient client = getClient();
		final Runnable periodicTask = new Runnable() {
			public void run() {
				try {
					BaseRspHandler handler = new WebServiceRspHandler(mediator);
					logger.debug("Sent a USERS REQuest");
					client.send("USERS REQ".getBytes(), handler);
					handler.waitForResponse();
				} catch (IOException e) {
					logger.warn("Could not send USERS REQ to web serivce");
				}
			}
		};
		periodicTaskHandle =
			scheduler.scheduleAtFixedRate(periodicTask, 0, POLLING_INTERVAL,
		                                  MILLISECONDS);
		logger.debug("Started polling for users from web service.");
	}

	public static void stopPollingForUsers() {
		if (periodicTaskHandle != null)
			periodicTaskHandle.cancel(true);
	}

	/* Announce web service about a new user being online. */
	public static void userIsOnline(String name, String host, int port) throws IOException {
		final BaseClient client = getClient();
		String portStr = Integer.toString(port);
		BaseRspHandler handler = new WebServiceRspHandler(null);
		String command = "USERS ADD " + name + " " + host + " " + portStr;
		client.send(command.getBytes(), handler);
		handler.waitForResponse();
	}

	/* Announce web service about a user's exit. */
	public static void userExits(String name) throws IOException {
		final BaseClient client = getClient();
		BaseRspHandler handler = new WebServiceRspHandler(null);
		String command = "USERS EXIT " + name;
		client.send(command.getBytes(), handler);
		handler.waitForResponse();
	}

	public static void sendUserTreeNode(String name, TreeNode root) throws IOException {
		byte[] serializedRoot = null;
		serializedRoot = SerializationHelper.serialize(root);
		BaseRspHandler handler = new WebServiceRspHandler(null);
		client.send(serializedRoot, handler);
		handler.waitForResponse();
	}

	public static void getUserTreeNode(final String name, final UIMediator uimed) throws IOException {
		System.out.println("Sending user tree node req for " + name);
		byte[] request = ("FILES REQ " + name).getBytes();

		/* A special handler that will deserialize in TreeNode back. */
		BaseRspHandler handler = new WebServiceRspHandler(null) {
			@Override
			protected void processResponse(byte[] rsp) {
				TreeNode root = null;
				try {
					root = (TreeNode) SerializationHelper.deserialize(rsp);
					logger.debug("Successfully received TreeNode class for user " + name);
					uimed.setCurrentUserFiles(root);
				} catch (ClassNotFoundException | IOException e) {
					logger.error(e.toString());
				}
			}
		};

		client.send(request, handler);
		handler.waitForResponse();
	}

	/* Create a client which connects to the port and host where the
	 * web service is listening.
	 */
	private static BaseClient getClient() throws FileNotFoundException, IOException {
		if (client == null) {
			/* Create the client only once, then reuse the instance. */
			Properties configs = getWebServiceConfig();
			String host = configs.getProperty("host", "localhost");
			InetAddress hostAddress = InetAddress.getByName(host);
			int port = Integer.parseInt(configs.getProperty("port", "9089"));
			client = new BaseClient(hostAddress, port);
			/* Also start the client, so we may use it with client.send. */
			Thread t = new Thread(client);
			//t.setDaemon(true);
			t.start();
		}
		return client;
	}

	private static Properties getWebServiceConfig() throws FileNotFoundException, IOException {
		/* Startup WebService using port and host from config. */
		Properties configs = new Properties();
		Path p = Paths.get("..", WEBSERVICE_FILENAME);
		configs.load(new FileInputStream(p.toString()));

		return configs;
	}

}
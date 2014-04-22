package webservice_client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.Logger;

import utils.BaseClient;
import utils.BaseRspHandler;

import main.Mediator;

import static java.util.concurrent.TimeUnit.*;

public class WebServiceClient {
	static Logger logger = Logger.getLogger(WebServiceClient.class);

	private final static String WEBSERVICE_FILENAME = "webservice.properties";

	// In milliseconds;
	private static final int POLLING_INTERVAL = 300;
	private static final ScheduledExecutorService scheduler =
			Executors.newScheduledThreadPool(1);

	/* Method which does polling for users list (from webservice)
	 * as a periodic task (at POLLING_INTERVAL ms), which never ends.
	 */
	public static void startPollingForUsers(final Mediator mediator) throws FileNotFoundException, IOException {
		final BaseClient client = getClient();
		new Thread(client).start();

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
		scheduler.scheduleAtFixedRate(periodicTask, 0, POLLING_INTERVAL,
		                              MILLISECONDS);
		logger.debug("Started polling for users from web service.");
	}

	/* Create a client which connects to the port and host where the
	 * web service is listening.
	 */
	private static BaseClient getClient() throws FileNotFoundException, IOException {
		Properties configs = getWebServiceConfig();
		String host = configs.getProperty("host", "localhost");
		InetAddress hostAddress = InetAddress.getByName(host);
		int port = Integer.parseInt(configs.getProperty("port", "9089"));
		return new BaseClient(hostAddress, port);
	}

	private static Properties getWebServiceConfig() throws FileNotFoundException, IOException {
		/* Startup WebService using port and host from config. */
		Properties configs = new Properties();
		Path p = Paths.get("..", WEBSERVICE_FILENAME);
		configs.load(new FileInputStream(p.toString()));

		return configs;
	}

}
package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import webservice.WebService;
import webservice.WebServiceWorker;

class Main {
	public final static String WEBSERVICE_FILENAME = "webservice.properties";

	static Logger logger = Logger.getLogger(Main.class);

	static void configureLogger(String logFile) {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.TRACE);

		// Create a console appender to be user when debugging.
		// TODO: Set the log level here to FATAL.
		ConsoleAppender console = new ConsoleAppender();
		// Configure the appender
		String PATTERN = "%d{ABSOLUTE} [%t|%C{1}|%M] %m%n";
		console.setLayout(new PatternLayout(PATTERN)); 
		console.setThreshold(Level.TRACE);
		console.activateOptions();
		// Add appender to Root Logger
		rootLogger.addAppender(console);

		RollingFileAppender fa = new RollingFileAppender();
		fa.setName("FileLogger");
		fa.setFile(logFile);
		fa.setLayout(new PatternLayout(PATTERN));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();
		fa.setMaxBackupIndex(5); // keep a maximum of 5 files
		fa.setMaxFileSize("100KB");
		rootLogger.addAppender(fa);
	}

	/**
	 * @param args the command line arguments
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		/* Configure logger. */
		String logFile = Paths.get("logs", "webservice.log").toString();
		configureLogger(logFile);

		/* Startup WebService using port and host from config. */
		Properties configs = new Properties();
		Path p = Paths.get("..", WEBSERVICE_FILENAME);
		configs.load(new FileInputStream(p.toString()));

		String host = configs.getProperty("host", "localhost");
		InetAddress hostAddress = InetAddress.getByName(host);
		int port = Integer.parseInt(configs.getProperty("port", "9089"));
		logger.debug("Starting webservice on port " + Integer.toString(port) +
		             ", host " + hostAddress.toString());

		try {
			WebServiceWorker worker = new WebServiceWorker();
			new Thread(worker).start();
			new Thread(new WebService(hostAddress, port, worker)).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
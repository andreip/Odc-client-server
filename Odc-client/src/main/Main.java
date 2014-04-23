package main;

import gui.UIMediator;
import gui.UserInterface;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import network.Network;
import network.NetworkWorker;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import webservice_client.WebServiceClient;

/**
 *
 * @author andrei
 */
public class Main {
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

	public static Network createNetwork(Properties configs, String username,
	                                    String homeDir, NetworkWorker worker,
	                                    Mediator mediator) {
		Network netInterface = null;

    	// Create a network interface instance.
    	// Replace default with data from configuration file.
    	InetAddress hostAddress = null;
    	int port = 9090;
    	if (configs.containsKey("host")) {
    		try {
				hostAddress = InetAddress.getByName(configs.getProperty("host"));
			} catch (UnknownHostException e) {
				logger.error("Unable to read host address " + configs.getProperty("host") + ". Using default.");
			}
    	} else {
    		logger.info("No config entry for host address. Using default.");
    	}
    	if (configs.containsKey("port")) {
    		try {
    			port = Integer.parseInt(configs.getProperty("port"));
    		} catch (NumberFormatException e) {
    			logger.error("Unable to read port number " + configs.getProperty("port") + ". Using default.");
    		}
    	} else {
    		logger.info("No config entry for port number. Using default.");
    	}
        try {
			netInterface = new Network(mediator, worker, hostAddress, port);
			netInterface.initSelector();
		} catch (IOException e) {
			logger.fatal("Unable to instantiate network interface. Error was: " + e.toString() + ".Shutting down...");
			System.exit(-1);
		}		
        
        return netInterface;
	}
	
    /**
     * @param args the command line arguments
     * @throws IOException 
     */
    public static void main(final String[] args) throws IOException {
    	if (args.length != 1) {
    		System.err.println("Usage: java Main <user_name>");
    		System.exit(-1);
    	}
    	// Extract information from the command line arguments
    	Properties configs = new Properties();
    	try {
			configs.load(new FileInputStream("res/" + args[0] + ".properties"));
		} catch (IOException e) {
			System.err.println("Unable to load config file for user " + args[0]);
			System.exit(-1);
		}

    	String homeDir = "res/" + args[0] + "/";
    	String logFile = "logs/" + args[0] + ".log";
    	configureLogger(logFile);

        // Announce WebService about the current user being online.
        try {
			WebServiceClient.userIsOnline(args[0], configs.getProperty("host"),
			                              Integer.parseInt(configs.getProperty("port")));
		} catch (NumberFormatException | IOException e) {
			System.err.println(e.toString());
			System.exit(-1);
		}

    	// Create the mediator and pass it the user info.
    	// The mediator handles all structure and role management.
        Mediator mediator = new Mediator(args[0], homeDir);
        NetworkWorker worker = new NetworkWorker(mediator);
        Network network = createNetwork(configs, args[0], homeDir, worker, mediator);
        mediator.initMediator(worker, network);
        
        // Get the UIMediator instance and pass it to the Mediator.
        UIMediator uiMediator = UIMediator.getInstance();
        mediator.registerUIMediator(uiMediator);

        UserInterface ui = new UserInterface(uiMediator);
        ui.setVisible(true);
        ui.setLocationRelativeTo(null);
        ui.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
	            logger.debug("User exits: " + args[0]);
	            try {
					WebServiceClient.userExits(args[0]);
				} catch (IOException e1) {
					logger.error(e1.getStackTrace());
				}
	            System.exit(0);
			}
		});
        
        // Start execution of the software.
        Thread t = new Thread(mediator);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {

        // FIXME: does not trigger when exit Ctrl+C: Announce WebService that this user exists.
        } finally {
            logger.debug("User exits: " + args[0]);
            WebServiceClient.userExits(args[0]);
        }
    }
}

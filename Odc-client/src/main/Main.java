package main;

import gui.UIMediator;
import gui.UserInterface;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrei
 */
public class Main {
	
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
     */
    public static void main(String[] args) {
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

    	// Create the mediator and pass it the user info.
    	// The mediator handles all structure and role management.
        Mediator mediator = new Mediator(configs, args[0], homeDir);
        
        // Get the UIMediator instance and pass it to the Mediator.
        UIMediator uiMediator = UIMediator.getInstance();
        mediator.registerUIMediator(uiMediator);

        UserInterface ui = new UserInterface(uiMediator);
        ui.setVisible(true);
        ui.setLocationRelativeTo(null);
        
        // Start execution of the software.
        new Thread(mediator).start();
    }
}

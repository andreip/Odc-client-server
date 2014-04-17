package main;

import gui.UIMediator;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import network.Network;
import network.NetworkWorker;

import org.apache.log4j.Logger;

/**
 *
 * @author Mariana
 */
public class Mediator implements Runnable {
	// Logger for this class 
	static Logger logger = Logger.getLogger(Mediator.class);

    UIMediator uiMediator;
    Network netInterface;
    NetworkWorker worker;
    private String username;
    private String homeDir;
    
    int command = 0;
    HashMap<TransferInfo, Integer> transfers = new HashMap<>();
    Map<String, Pair> users = new HashMap<>();
    Random rand = new Random();

    public Mediator(String username, String homeDir) {
    	this.username = username;
    	this.homeDir = homeDir;
    }
    
    public void initMediator(NetworkWorker worker, Network network) {
    	// Create a network worker.
    	this.worker = worker;
    	new Thread(worker).start();
    	this.netInterface = network;
        new Thread(netInterface).start();
    }
    
    public String getUsername() {
    	return this.username;
    }
    
    public String getHomedir() {
    	return this.homeDir;
    }
    
    public void registerUIMediator(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        this.uiMediator.registerMediator(this);
    }
    
    public TransferInfo newOutgoingTransfer(String toUser, String filePath) {
    	logger.debug("New outgoing transfer.");
    	final TransferInfo ti = new TransferInfo();
        String[] splitPath = filePath.split("/");
        String filename = splitPath[splitPath.length - 1]; // filename is last
        String path = this.homeDir;
        for (int i = 0; i < splitPath.length - 1; i++) { // path is all without last
        	path += splitPath[i] + "/";
        }
        ti.filename = filename;
        ti.path = path;
        ti.filesize = (int) new File(ti.path + ti.filename).length();
        ti.state = "Starting...";
        ti.userFrom = "me";
        ti.userTo = toUser;
        transfers.put(ti, 0);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	uiMediator.newOutgoingTransfer(ti);
            }
        });
        return ti;
    }

    public void notifyTransferFilesize(final TransferInfo ti) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	uiMediator.updateTransferFilesize(ti.id, ti.filesize);
            }
        });
    }
    
    public void updateTransferValue(final TransferInfo ti, final int progress) {
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	uiMediator.updateTransferValue(ti.id, progress);
            }
        });
    }

    public void newIncomingTransfer(TransferInfo ti) {
    	logger.debug("New incoming transfer.");
    	Pair netInfo = users.get(ti.userFrom);
        try {
			this.netInterface.startTransfer(ti, InetAddress.getByName((String) netInfo.first),
												Integer.parseInt((String) netInfo.second));
		} catch (NumberFormatException | UnknownHostException e) {
			notifyNetworkError("Unable to initiate file download!");
			logger.error("Unable to start transfer. Error was: " + e.toString());
		}
    }
    
    public void notifyNetworkError(String message) {
    	this.uiMediator.notifyError(message);
    }

	@Override
	public void run() {
		try {
			Scanner s = new Scanner(new File("res/users"));
			while (s.hasNext()) {
				String line = s.nextLine();
				String[] contents = line.split(" ");
				String user = contents[0];
				this.users.put(user, new Pair(contents[1], contents[2]));
				if (!user.equals(this.username)) {
					uiMediator.userOn(user);
				}
			}
			s.close();
		} catch (FileNotFoundException e) {
			logger.error("Unable to get users file. Error was: " + e.toString());
		}

		while(true) {}
	}
}

class Pair {
    public Object first;
    public Object second;

    public Pair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }
}
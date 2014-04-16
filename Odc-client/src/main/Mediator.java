package main;

import gui.UIMediator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    public Mediator(Properties configs, String username, String homeDir) {
    	this.username = username;
    	this.homeDir = homeDir;
    	
    	// Create a network worker.
    	worker = new NetworkWorker(this);
    	new Thread(worker).start();
    	
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
			netInterface = new Network(this, worker, hostAddress, port);
		} catch (IOException e) {
			logger.fatal("Unable to instantiate network interface. Error was: " + e.toString() + ".Shutting down...");
			System.exit(-1);
		}
        
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

    public void notifyTransferFilesize(TransferInfo ti) {
    	final TransferInfo tii = ti;
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	uiMediator.updateTransferFilesize(tii.id, tii.filesize);
            }
        });
    }
    
    public void updateTransferValue(TransferInfo ti, final int progress) {
    	final TransferInfo tii = ti;
    	SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	uiMediator.updateTransferValue(tii.id, progress);
            }
        });
    }
/*
    protected Integer doInBackground() {
        while (true) {

            switch(this.command % 7) {
                case 6: // New outgoing transfer
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TransferInfo ti = new TransferInfo();
                            ti.filename = "test.txt";
                            ti.filesize = rand.nextInt(20);
                            ti.state = "Starting...";
                            ti.userFrom = "me";
                            //ti.userTo = users.get(rand.nextInt(users.size() - 1));
                            transfers.put(ti, 0);
                            uiMediator.newOutgoingTransfer(ti);
                        }
                    });
                    break;
                case 0:
                case 4:
                    final String nextUser = "Ana-" + rand.nextInt(30);
                    if (!users.contains(nextUser)) {
                        users.add(nextUser);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                uiMediator.userOn(nextUser);
                                uiMediator.updateState("Receiving user list...");
                            }
                        });
                    }
                    break;
                case 2:
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (users.size() > 1) {
                                uiMediator.userOff(users.get(rand.nextInt(users.size() - 1)));
                            }
                        }
                    });
                    break;
                case 1:
                case 3:
                case 5:
                    uiMediator.updateState("Transfering...");
                    for (TransferInfo ti : this.transfers.keySet()) {
                        int i = this.transfers.get(ti) + 1;
                        if (i <= ti.filesize) {
                            //publish(new Pair(ti.id, i));
                            this.transfers.put(ti, i);
                        }
                    }
                    break;
            }
            command += 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                logger.debug("Thread interrupted.");
            }
        }
    }
*/
    protected void process(List<Pair> chunks) {
        for (Pair chunk : chunks) {
            uiMediator.updateTransferValue((Integer)chunk.first, (Integer)chunk.second);
        }
    }

    public void newIncomingTransfer(TransferInfo ti) {
    	logger.debug("New incoming transfer.");
    	Pair netInfo = users.get(ti.userFrom);
        try {
			this.netInterface.startTransfer(ti, InetAddress.getByName((String) netInfo.first),
												Integer.parseInt((String) netInfo.second));
		} catch (NumberFormatException | UnknownHostException e) {
			e.printStackTrace();
		}
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
			e.printStackTrace();
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
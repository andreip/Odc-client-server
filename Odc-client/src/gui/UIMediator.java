package gui;


import java.io.File;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import main.Mediator;
import main.TransferInfo;
import models.TransfersTableModel;
import models.UserFilesTreeModel;
import models.UserListModel;

import org.apache.log4j.Logger;

/**
 *
 * @author Mariana
 */
public class UIMediator {
    /* Add an instance so the mediator becomes a singleton. */
    private static UIMediator instance = null;
	// Logger for this class 
	static Logger logger = Logger.getLogger(UIMediator.class);

    private Mediator mediator;
    private JLabel status;
    private TransfersTableModel transfersTableModel;
    private UserListModel userListModel;
    private UserList userList;
    private UserFilesTreeModel userFilesTreeModel;
    private UserInterface ui;

    private UIMediator () {}

    public static UIMediator getInstance() {
        if (instance == null)
            instance = new UIMediator();
        return instance;
    }

    public void registerMediator(Mediator mediator) {
        this.mediator = mediator;
    }
    public void registerTransfersTableModel(TransfersTableModel transfersTableModel) {
        this.transfersTableModel = transfersTableModel;
        logger.debug("Transfer table model registered.");
    }
    public void registerUserListModel(UserListModel userListModel) {
        this.userListModel = userListModel;
        logger.debug("User list model registered.");
    }
    public void registerUserList(UserList userList) {
        this.userList = userList;
        logger.debug("User list registered.");
    }
    public void registerUserFilesTreeModel(UserFilesTreeModel userFilesTreeModel) {
        this.userFilesTreeModel = userFilesTreeModel;
        logger.debug("Files tree model registered.");
    }
    public void registerStatusLabel(JLabel status) {
        this.status = status;
    }
    public void registerUserInterface(UserInterface ui) {
        this.ui = ui;
    }
    
    public String getUsername() {
    	if (this.mediator != null) {
    		return this.mediator.getUsername();
    	}
    	return "";
    }

    public void addUser(String username) {
        if (this.userListModel != null) {
            this.userListModel.addElement(username);
        }
    }

    public DefaultMutableTreeNode getUserHomeRoot(File rootFile) {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFile.getName());
        if (rootFile.isDirectory()) {
            for (File child : rootFile.listFiles()) {
                rootNode.add(getUserHomeRoot(child));
            }
        }
        return rootNode;
    }

    public void removeUser(String username) {
        if (this.userListModel != null) {
            this.userListModel.removeElement(username);
        }
    }

    /* TODO mariana: should get the file structure from the webservice. */
    public void setCurrentUserFiles(String username) {
        if (this.userFilesTreeModel != null) {
            TreeNode root = getUserHomeRoot(new File("res/" + username));
            this.userFilesTreeModel.setRoot(root);
        }
    }

    public void userOn(String username) {
        if (this.userListModel != null) {
            this.addUser(username);
        }
    }

    public void userOff(String username) {
        if (this.userListModel != null) {
            this.removeUser(username);
        }
    }

    public void updateState(String state) {
        if (this.status != null) {
            this.status.setText(state);
        }
    }

    /* Another user wants to download a file from this user. */
    public void newOutgoingTransfer(TransferInfo info) {
        if (this.transfersTableModel != null && info != null) {
            this.transfersTableModel.addRow(info);
        }
    }

    /* This user wants to download a file from another user. */
    public void newIncomingTransfer(String filePath) {
    	final String user = this.userList.getSelectedUser();
        String[] splitPath = filePath.split("/");
        final String filename = splitPath[splitPath.length - 1];
        String path = "";
        for (int i = 1; i < splitPath.length - 1; i++) {
        	path += splitPath[i] + "/";
        }
        final String fp = path;
        logger.debug("File to download has path=" + fp + " and name=" + filename);
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TransferInfo ti = new TransferInfo();
                    ti.filename = filename;
                    ti.filesize = 0;
                    ti.path = fp;
                    ti.state = "Starting...";
                    ti.userFrom = user;
                    ti.userTo = "me";
                    transfersTableModel.addRow(ti);
                    mediator.newIncomingTransfer(ti);
                    logger.debug("Sent TransferInfo to mediator.");
                }
            });
    }

    public void updateTransferValue(int id, int value) {
        if (this.transfersTableModel != null) {
            this.transfersTableModel.updateTransferValue(id, value);
        }
    }
    
    public void updateTransferFilesize(int id, int value) {
        if (this.transfersTableModel != null) {
            this.transfersTableModel.updateTransferFilesize(id, value);
        }
    }
    public void notifyError(final String message) {
	    SwingUtilities.invokeLater(new Runnable() {
	        @Override
	        public void run() {
	        	JOptionPane.showMessageDialog(UIMediator.this.ui, message);
	        }
	    });
    }

    public void repaintUI() {
        this.ui.repaint();
    }
}

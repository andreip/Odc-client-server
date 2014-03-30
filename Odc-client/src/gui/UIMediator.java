package gui;


import java.util.HashMap;
import java.util.Random;
import models.*;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import main.Mediator;
import main.TransferInfo;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mariana
 */
public class UIMediator {
    /* Add an instance so the mediator becomes a singleton. */
    private static UIMediator instance = null;

    private Mediator mediator;
    private HashMap<String, TreeNode> userFilesMap;
    private JLabel status;
    private TransfersTableModel transfersTableModel;
    private UserListModel userListModel;
    private UserList userList;
    private UserFilesTreeModel userFilesTreeModel;
    private UserInterface ui;

    public UIMediator () {
        userFilesMap = new HashMap<>();
    }

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
        System.out.println("added transfers table");
    }
    public void registerUserListModel(UserListModel userListModel) {
        this.userListModel = userListModel;
        this.addUser("user1");
        this.addUser("user2");
        System.out.println("added users");
    }
    public void registerUserList(UserList userList) {
        this.userList = userList;
    }
    public void registerUserFilesTreeModel(UserFilesTreeModel userFilesTreeModel) {
        this.userFilesTreeModel = userFilesTreeModel;
        System.out.println("register files tree model");
    }
    public void registerStatusLabel(JLabel status) {
        this.status = status;
    }
    public void registerUserInterface(UserInterface ui) {
        this.ui = ui;
    }

    /* TODO andrei: a user should have some kind of info about
     * its files, a TreeNode.
     */
    public void addUser(String userName) {
        if (this.userListModel != null) {
            this.userListModel.addElement(userName);
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(userName);
            DefaultMutableTreeNode innerFolder = new DefaultMutableTreeNode(userName);
            innerFolder.add(new DefaultMutableTreeNode(userName + ".txt"));
            root.add(innerFolder);
            this.userFilesMap.put(userName, root);
        }
    }

    public void removeUser(String userName) {
        if (this.userListModel != null) {
            this.userListModel.removeElement(userName);
            this.userFilesMap.remove(userName);
        }
    }

    public void setCurrentUserFiles(String userName) {
        if (this.userFilesTreeModel != null) {
            TreeNode root = this.userFilesMap.get(userName);
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
        if (this.transfersTableModel != null) {
            this.transfersTableModel.addRow(info);
        }
    }

    /* This user wants to download a file from another user. */
    public void newIncomingTransfer(String filePath) {
        final String user = this.userList.getSelectedUser();
        final String filename = filePath.split("/")[filePath.split("/").length - 1];
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    TransferInfo ti = new TransferInfo();
                    ti.filename = filename;
                    ti.filesize = (new Random()).nextInt(20);
                    ti.state = "Starting...";
                    ti.userFrom = user;
                    ti.userTo = "me";
                    transfersTableModel.addRow(ti);
                    mediator.download(ti);
                }
            });
    }

    public void updateTransferValue(int id, int value) {
        if (this.transfersTableModel != null) {
            this.transfersTableModel.updateTransferValue(id, value);
        }
    }

    public void updateTransferState(int id, String state) {
        if (this.transfersTableModel != null) {
            this.transfersTableModel.updateTransferState(id, state);
        }
    }

    public void repaintUI() {
        this.ui.repaint();
    }
}

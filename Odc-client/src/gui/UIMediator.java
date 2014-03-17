package gui;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import models.*;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
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

    private HashMap<String, TreeNode> userFilesMap;
    private JLabel status;
    private TransfersTableModel transfersTableModel;
    private UserListModel userListModel;
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

    public void registerTransfersTableModel(TransfersTableModel transfersTableModel) {
        this.transfersTableModel = transfersTableModel;
        this.transfersTableModel.addRow(new Object[]{"Ana", "Ion", "info.txt", new JProgressBar(), "Completed."});
        System.out.println("added");
    }
    public void registerUserListModel(UserListModel userListModel) {
        this.userListModel = userListModel;
        this.addUser("user1");
        this.addUser("user2");
        System.out.println("added users");
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
    public TransferInfo newIncomingTransfer() {
        return null;
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

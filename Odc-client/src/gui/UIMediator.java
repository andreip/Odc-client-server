package gui;


import java.util.HashMap;
import models.*;
import javax.swing.JProgressBar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

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

    private TransfersTableModel transfersTableModel;
    private UserListModel userListModel;
    private UserFilesModel userFilesModel;
    private UserFilesTreeModel userFilesTreeModel;
    private HashMap<String, TreeNode> userFilesMap =
        new HashMap<String, TreeNode>();


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
    public void registerUserFilesModel(UserFilesModel userFilesModel) {
        this.userFilesModel = userFilesModel;
        this.userFilesModel.addElement("file1");
        this.userFilesModel.addElement("file2");
        System.out.println("added files for user");
    }
    public void registerUserFilesTreeModel(UserFilesTreeModel userFilesTreeModel) {
        this.userFilesTreeModel = userFilesTreeModel;
        System.out.println("register files tree model");
    }

    /* TODO andrei: a user should have some kind of info about
     * its files, a TreeNode.
     */
    public void addUser(String userName) {
        this.userListModel.addElement(userName);
        TreeNode root = new DefaultMutableTreeNode(userName);
        this.userFilesMap.put(userName, root);
    }
}

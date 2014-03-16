package gui;


import models.*;
import javax.swing.JProgressBar;

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
        this.userListModel.addElement("user1");
        this.userListModel.addElement("user2");
        System.out.println("added users");
    }
    public void registerUserFilesModel(UserFilesModel userFilesModel) {
        this.userFilesModel = userFilesModel;
        this.userFilesModel.addElement("file1");
        this.userFilesModel.addElement("file2");
        System.out.println("added files for user");
    }
}

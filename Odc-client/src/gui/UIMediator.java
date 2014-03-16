package gui;


import models.TransfersTableModel;
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
    TransfersTableModel transfersTableModel;
    /* Add an instance so the mediator becomes a singleton. */
    private static UIMediator instance = null;

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
}

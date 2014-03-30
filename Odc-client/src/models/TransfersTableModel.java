package models;


import gui.UIMediator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;
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
public class TransfersTableModel extends DefaultTableModel {
    UIMediator uiMediator;
    private List<TransferInfo> transfers;
    private HashMap<Integer, JProgressBar> progressBars;

    public TransfersTableModel(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        this.addColumn("Source");
        this.addColumn("Destination");
        this.addColumn("Filename");
        this.addColumn("Progress");
        this.addColumn("Status");
        this.uiMediator.registerTransfersTableModel(this);
        progressBars = new HashMap<>();
        transfers = new LinkedList<>();
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void addRow(TransferInfo data) {
        JProgressBar bar = new JProgressBar(0, data.filesize);
        super.addRow(new Object[] { data.userFrom,
                                    data.userTo,
                                    data.filename,
                                    bar,
                                    data.state });
        transfers.add(data);
        data.id = transfers.indexOf(data);
        progressBars.put(data.id, bar);
    }

    public void updateTransferValue(int id, int value) {
        progressBars.get(id).setValue(value);
        if (value == 1) {
            updateTransferState(id, "Transfering...");
        }
        if (progressBars.get(id).getMaximum() == value) {
            updateTransferState(id, "Completed.");
        }
        this.uiMediator.repaintUI();
        progressBars.get(id).update(progressBars.get(id).getGraphics());
    }

    public void updateTransferState(int id, String state) {
        super.setValueAt(state, id, 4);
    }
}

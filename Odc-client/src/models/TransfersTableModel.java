package models;


import gui.UIMediator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;

import main.TransferInfo;

import org.apache.log4j.Logger;

/**
 *
 * @author Mariana
 */
public class TransfersTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	// Logger for this class 
	static Logger logger = Logger.getLogger(TransfersTableModel.class);

	final public static String completedState = "Completed.";
	final public static String transferingState = "Transfering...";
	
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
    public Class<?> getColumnClass(int c) {
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
        logger.debug("Row added - from " + data.userFrom + ", to " + data.userTo + ", " + data.filename);
    }

    public void updateTransferValue(int id, int value) {
        progressBars.get(id).setValue(value);
        String transferText = getTransferStateForValue(value,
            progressBars.get(id).getMaximum());
        updateTransferState(id, transferText);
        progressBars.get(id).update(progressBars.get(id).getGraphics());
        this.uiMediator.repaintUI();
    }

    public void updateTransferFilesize(int id, int value) {
        progressBars.get(id).setMaximum(value);
        updateTransferState(id, "Got filesize...");
        this.uiMediator.repaintUI();
        progressBars.get(id).update(progressBars.get(id).getGraphics());
    }

    public String getTransferStateForValue(int value, int completed) {
        return (value >= completed) ? completedState : transferingState;
    }

    public void updateTransferState(int id, String state) {
        super.setValueAt(state, id, 4);
    }
}

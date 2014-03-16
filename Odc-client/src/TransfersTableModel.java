
import javax.swing.table.DefaultTableModel;

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

    public TransfersTableModel(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        this.addColumn("Source");
        this.addColumn("Destination");
        this.addColumn("Filename");
        this.addColumn("Progress");
        this.addColumn("Status");
        this.uiMediator.registerTransfersTableModel(this);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    @Override
    public void addRow(Object[] rowData) {
        super.addRow(rowData); //To change body of generated methods, choose Tools | Templates.
    }
}

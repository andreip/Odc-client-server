package gui;


import java.awt.Component;
import models.TransfersTableModel;
import models.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Mariana
 */
public class UserInterface extends JFrame {

    UIMediator uiMediator;
    /**
     * Creates new form GUI
     */
    public UserInterface(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        initComponents();
        
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AutoGeneratedGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(AutoGeneratedGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AutoGeneratedGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(AutoGeneratedGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")                       
    private void initComponents() {

        statusLabel = new JLabel();
        mainSplitPane = new JSplitPane();
        leftSplitPane = new JSplitPane();
        filesScrollPane = new JScrollPane();
        filesList = new JList();
        transfersScrollPane = new JScrollPane();
        transfersTable = new JTable();
        usersScrollPane = new JScrollPane();
        usersList = new JList();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(600, 400));
        setPreferredSize(new java.awt.Dimension(600, 400));

        statusLabel.setText("");
        uiMediator.registerStatusLabel(statusLabel);

        mainSplitPane.setDividerLocation(480);
        mainSplitPane.setResizeWeight(1.0);

        leftSplitPane.setDividerLocation(200);
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(0.5);

        filesList.setModel(new UserFilesModel(uiMediator));
        filesScrollPane.setViewportView(filesList);

        leftSplitPane.setLeftComponent(filesScrollPane);

        transfersTable.setModel(new TransfersTableModel(uiMediator));
        transfersTable.setDefaultRenderer(JProgressBar.class,
                new TableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(
                                JTable table, Object value, boolean isSelected,
                                boolean hasFocus, int row, int column) {
                            return (JProgressBar) value;
                        }
                });
        transfersScrollPane.setViewportView(transfersTable);

        leftSplitPane.setRightComponent(transfersScrollPane);

        mainSplitPane.setLeftComponent(leftSplitPane);

        usersList.setModel(new UserListModel(uiMediator));
        usersScrollPane.setViewportView(usersList);

        mainSplitPane.setRightComponent(usersScrollPane);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(statusLabel)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(mainSplitPane, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(mainSplitPane, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusLabel))
        );

        pack();
    }

    // Variables declaration - do not modify                     
    private JList filesList;
    private JScrollPane filesScrollPane;
    private JSplitPane leftSplitPane;
    private JSplitPane mainSplitPane;
    private JLabel statusLabel;
    private JScrollPane transfersScrollPane;
    private JTable transfersTable;
    private JList usersList;
    private JScrollPane usersScrollPane;
    // End of variables declaration                   
}

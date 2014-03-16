
import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
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

        statusLabel.setText("jLabel1");

        mainSplitPane.setDividerLocation(480);
        mainSplitPane.setResizeWeight(1.0);

        leftSplitPane.setDividerLocation(200);
        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(0.5);

        filesList.setModel(new AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        filesScrollPane.setViewportView(filesList);

        leftSplitPane.setLeftComponent(filesScrollPane);

        transfersTable.setModel(new TransfersTableModel(uiMediator));
        transfersTable.setDefaultRenderer(JProgressBar.class,
                new ProgressTableRenderer());
        transfersScrollPane.setViewportView(transfersTable);

        leftSplitPane.setRightComponent(transfersScrollPane);

        mainSplitPane.setLeftComponent(leftSplitPane);

        usersList.setModel(new AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new UserInterface(UIMediator.getInstance()).setVisible(true);
            }
        });
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

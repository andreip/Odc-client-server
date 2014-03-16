
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Renders the JProgressBars from the transfers table.
 * 
 * Holds a list of JProgressBars, and renders the proper one for the given row.
 * @author Mariana
 */
public class ProgressTableRenderer implements TableCellRenderer {
    List<JProgressBar> progrssBars = new LinkedList<>();
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return (JProgressBar) value;
    }
    /*
    void add(JProgressBar bar) {
        this.progrssBars.add(bar);
    }
    
    void add(int index, JProgressBar bar) {
        this.progrssBars.add(index, bar);
    }
    
    void remove(JProgressBar bar) {
        this.progrssBars.remove(bar);
    }
    
    void remove(int index) {
        this.progrssBars.remove(index);
    }
    */
}

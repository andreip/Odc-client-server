package gui;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author andrei
 */
public class UserList extends JList<String> {
	private static final long serialVersionUID = 1L;
	UIMediator uiMediator;
    int lastSelectedIndex = -1;

    public UserList(final UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        uiMediator.registerUserList(this);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        /* Catch selection event on user list. This
         * will show the selected user's files.
         */
        this.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent le) {
                int index = UserList.this.getSelectedIndex();
                if (index != lastSelectedIndex) {
                    lastSelectedIndex = index;
                    String userName = (String) UserList.this.getSelectedValue();
                    uiMediator.setCurrentUserFiles(userName);
                }
            }
        });
    }

    public String getSelectedUser() {
        return (String)this.getSelectedValue();
    }
}
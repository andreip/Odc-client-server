package gui;

import java.io.IOException;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import webservice_client.WebServiceClient;

/**
 *
 * @author andrei
 */
public class UserList extends JList<String> {
	static Logger logger = Logger.getLogger(UIMediator.class);

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

                    if (userName == null) {
                    	uiMediator.setCurrentUserFiles(null);
                    	return;
                    }

                    /* Make WebService request for user files. */
                    try {
						WebServiceClient.getUserTreeNode(userName, uiMediator);
					} catch (IOException e) {
						logger.error(e.toString());
					}
                }
            }
        });
    }

    public String getSelectedUser() {
        return (String)this.getSelectedValue();
    }
}
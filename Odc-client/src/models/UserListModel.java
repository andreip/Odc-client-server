package models;


import gui.UIMediator;

import javax.swing.DefaultListModel;

/**
 *
 * @author andrei
 */
public class UserListModel extends DefaultListModel<String> {
	private static final long serialVersionUID = 1L;
	UIMediator uiMediator;
    
    public UserListModel(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        this.uiMediator.registerUserListModel(this);
    }
}

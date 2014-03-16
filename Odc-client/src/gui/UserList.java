/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author andrei
 */
public class UserList extends JList {
    UIMediator uimed;
    int lastSelectedIndex = -1;

    public UserList(final UIMediator uimed) {
        this.uimed = uimed;
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
                    uimed.setCurrentUserFiles(userName);
                }
            }
        });
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JList;

/**
 *
 * @author andrei
 */
public class UserList extends JList {
    UIMediator uimed;

    public UserList(final UIMediator uimed) {
        this.uimed = uimed;

        /* Catch doble-click event on user list. This
         * will show the user's files.
         */
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked on users");
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    String userName = (String) UserList.this.getSelectedValue();
                    uimed.setCurrentUserFiles(userName);
                }
            }
        });
    }
}

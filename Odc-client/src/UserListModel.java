
import javax.swing.DefaultListModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrei
 */
public class UserListModel extends DefaultListModel<String> {
    UIMediator uimed;
    
    public UserListModel(UIMediator uimed) {
        this.uimed = uimed;
        this.addElement("user1");
        this.addElement("user2");
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import gui.UIMediator;
import javax.swing.DefaultListModel;

/**
 *
 * @author andrei
 */
public class UserFilesModel extends DefaultListModel<String> {
    UIMediator uimed;

    public UserFilesModel(UIMediator uimed) {
        this.uimed = uimed;
        this.uimed.registerUserFilesModel(this);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import gui.UIMediator;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 *
 * @author andrei
 */
public class UserFilesTreeModel extends DefaultTreeModel {
    UIMediator uimed;
    
    public UserFilesTreeModel(TreeNode name, UIMediator uimed) {
        super(name);
        this.uimed = uimed;
        this.uimed.registerUserFilesTreeModel(this);
        System.out.println("instance of user tree");
    }
}
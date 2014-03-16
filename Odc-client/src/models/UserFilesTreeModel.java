/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package models;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author andrei
 */
public class UserFilesTreeModel extends DefaultTreeModel {
    
    public UserFilesTreeModel(TreeNode name) {
        super(name);
        System.out.println("instance of user tree");
    }
}
package models;

import gui.UIMediator;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 *
 * @author andrei
 */
public class UserFilesTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = 1L;
	UIMediator uiMediator;
    
    public UserFilesTreeModel(TreeNode name, UIMediator uiMediator) {
        super(name);
        this.uiMediator = uiMediator;
        this.uiMediator.registerUserFilesTreeModel(this);
    }
}
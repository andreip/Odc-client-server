package main;

import gui.UIMediator;
import gui.UserInterface;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author andrei
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UIMediator uiMediator = UIMediator.getInstance();
        Mediator mediator = new Mediator(uiMediator);
        mediator.execute();

        UserInterface ui = new UserInterface(uiMediator);
        ui.setVisible(true);
        ui.setLocationRelativeTo(null);
    }
}

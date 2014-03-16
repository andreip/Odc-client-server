/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import gui.UIMediator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Mariana
 */
public class Mediator extends SwingWorker<Object, Integer> {
    private static final int DELAY = 1000;
    UIMediator uiMediator;
    
    public Mediator(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
    }
    
    @Override
    protected Integer doInBackground() {
        while (true) {
            try {
                uiMediator.userOn("Ana");
                uiMediator.updateState("Receiving user list...");
                publish(1);
                setProgress(1);
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
            for (Integer chunk : chunks) {
                    System.out.println(chunk);
            }
    }

    @Override
    protected void done() {
      if (isCancelled())
        System.out.println("Cancelled !");
      else
        System.out.println("Done !");
    }
}
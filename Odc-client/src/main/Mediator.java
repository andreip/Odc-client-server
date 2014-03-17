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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Mariana
 */
public class Mediator extends SwingWorker<Object, Integer> {
    private static final int DELAY = 1000;
    UIMediator uiMediator;
    TransferInfo ti = new TransferInfo();
    
    public Mediator(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
    }
    
    @Override
    protected Integer doInBackground() {
        ti.filename = "test.txt";
        ti.filesize = 10;
        ti.state = "";
        ti.userFrom = "Ana";
        ti.userTo = "Ion";
        System.out.println("hellooooo");
        while (true) {
            try {
                Thread.sleep(DELAY);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        uiMediator.newOutgoingTransfer(ti);
                    }
                });
                
                System.out.println("before for");
                for (int i = 0; i <= ti.filesize; i += 1) {
                    publish(i);
                    System.out.println("+++ " + Thread.currentThread());
                    Thread.sleep(DELAY);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
        final Integer chunk = chunks.get(chunks.size() - 1);
        uiMediator.updateTransferValue(ti.id, chunk);
        System.out.println("--- " + Thread.currentThread());
    }

    @Override
    protected void done() {
      if (isCancelled())
        System.out.println("Cancelled !");
      else
        System.out.println("Done !");
    }
}
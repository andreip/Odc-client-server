/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package main;

import gui.UIMediator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Mariana
 */
public class Mediator extends SwingWorker<Object, Pair> {
    private static final int DELAY = 1000;
    UIMediator uiMediator;
    int command = 0;
    HashMap<TransferInfo, Integer> transfers = new HashMap<>();
    List<String> users = new LinkedList<>();
    Random rand = new Random();

    public Mediator(UIMediator uiMediator) {
        this.uiMediator = uiMediator;
        this.uiMediator.registerMediator(this);
    }

    @Override
    protected Integer doInBackground() {
        while (true) {

            switch(this.command % 7) {
                case 6: // New outgoing transfer
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            TransferInfo ti = new TransferInfo();
                            ti.filename = "test.txt";
                            ti.filesize = rand.nextInt(20);
                            ti.state = "Starting...";
                            ti.userFrom = "me";
                            ti.userTo = users.get(rand.nextInt(users.size() - 1));
                            transfers.put(ti, 0);
                            uiMediator.newOutgoingTransfer(ti);
                        }
                    });
                    break;
                case 0:
                case 4:
                    final String nextUser = "Ana-" + rand.nextInt(30);
                    if (!users.contains(nextUser)) {
                        users.add(nextUser);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                uiMediator.userOn(nextUser);
                                uiMediator.updateState("Receiving user list...");
                            }
                        });
                    }
                    break;
                case 2:
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (users.size() > 1) {
                                uiMediator.userOff(users.get(rand.nextInt(users.size() - 1)));
                            }
                        }
                    });
                    break;
                case 1:
                case 3:
                case 5:
                    uiMediator.updateState("Transfering...");
                    for (TransferInfo ti : this.transfers.keySet()) {
                        int i = this.transfers.get(ti) + 1;
                        if (i <= ti.filesize) {
                            publish(new Pair(ti.id, i));
                            this.transfers.put(ti, i);
                        }
                    }
                    break;
            }
            command += 1;
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException ex) {
                Logger.getLogger(Mediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    protected void process(List<Pair> chunks) {
        for (Pair chunk : chunks) {
            uiMediator.updateTransferValue(chunk.transferId, chunk.progress);
        }
    }

    @Override
    protected void done() {
      if (isCancelled())
        System.out.println("Cancelled !");
      else
        System.out.println("Done !");
    }

    public void download(TransferInfo ti) {
        this.transfers.put(ti, 0);
    }
}

class Pair {
    public Integer transferId;
    public Integer progress;

    public Pair(Integer transferId, Integer progress) {
        this.transferId = transferId;
        this.progress = progress;
    }
}
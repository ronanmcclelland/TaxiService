/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taxiservice;

// ManU Supporter process
import java.util.Random;
import java.util.concurrent.atomic.*;

class ManSupporterProcess extends Thread {

    private volatile boolean stopFlag;

    private final MageeSemaphore manQueueSem, livQueueSem;
    private final AtomicInteger numManInTaxi, numLivInTaxi;
    private final MageeSemaphore taxiFullSem;
    private final MageeSemaphore mutexSem;
    private final String nameOfSupporter;
    private final Activity activities;

    //initialise (constructor)   
    public ManSupporterProcess(MageeSemaphore manQueueSem, MageeSemaphore livQueueSem,
            AtomicInteger numManInTaxi, AtomicInteger numLivInTaxi,
            MageeSemaphore taxiFullSem, MageeSemaphore mutexSem,
            String nameOfSupporter, Activity activities) {
        this.manQueueSem = manQueueSem;
        this.livQueueSem = livQueueSem;
        this.numManInTaxi = numManInTaxi;
        this.numLivInTaxi = numLivInTaxi;
        this.taxiFullSem = taxiFullSem;
        this.mutexSem = mutexSem;
        this.nameOfSupporter = nameOfSupporter;
        this.activities = activities;
        this.stopFlag = false;
    }

    @Override
    public void run() {  // start ManU supporter process
        while (!stopFlag) {
            manQueueSem.P(); // wait to get called forward from the queue 
            if (!stopFlag && !(this.isInterrupted())) {
                CDS.idleQuietly(100); // time to walk to taxi door
                activities.addActivity(nameOfSupporter);// record supporter getting taxi
                mutexSem.P(); //mutually exclusive access to global variables numManInTaxi and numLivInTaxi 
                int numMan = numManInTaxi.incrementAndGet(); // increment numManInTaxi and get
                int numLiv = numLivInTaxi.get(); // get numLivInTax      
                if (numMan + numLiv < 4)// still some space in the taxi
                {
                    if (numMan == 3) {
                        manQueueSem.V(); // signal a ManU supporter    
                    } else if ((numMan == 2) && (numLiv == 1)) {
                        livQueueSem.V(); // signal a Liverpool supporter             
                    } else if ((numMan == 1) && (numLiv == 2)) {
                        manQueueSem.V(); // signal a Liverpool                
                    } else { // signal either queue randomly
                        Random rand = new Random();
                        int r = rand.nextInt(2);
                        if (r == 0) {
                            manQueueSem.V();
                        } else {
                            livQueueSem.V();
                        }
                    }
                } else { //taxi full so signal taxi driver
                    taxiFullSem.V();
                }
                mutexSem.V(); // release mutually exclusive access
            } // end if
            CDS.idleQuietly(500); // time to find seat in taxi 
        } // end while
    } // end run

    // used to stop the thread
    public void stopThread() {
        stopFlag = true;
        this.interrupt();
    }

} // end ManU supporter process

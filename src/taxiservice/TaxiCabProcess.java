/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taxiservice;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class TaxiCabProcess extends Thread {

    private static final int NUM_TRIPS = 10;

    private final MageeSemaphore manQueueSem, livQueueSem;
    private final MageeSemaphore taxiFullSem;
    private final Activity activities;
    private final MageeSemaphore mutexSem;
    private final AtomicInteger numManInTaxi, numLivInTaxi;

    //initialise (constructor)   
    public TaxiCabProcess(MageeSemaphore manQueueSem, MageeSemaphore livQueueSem,
            AtomicInteger numManInTaxi, AtomicInteger numLivInTaxi,
            MageeSemaphore taxiFullSem, MageeSemaphore mutexSem, Activity activities) {
        this.manQueueSem = manQueueSem;
        this.livQueueSem = livQueueSem;
        this.numManInTaxi = numManInTaxi;
        this.numLivInTaxi = numLivInTaxi;
        this.taxiFullSem = taxiFullSem;
        this.mutexSem = mutexSem;
        this.activities = activities;
    }

    @Override
    public void run() {  // start taxi  process
        for (int trip = 0; trip < NUM_TRIPS; trip++) {
            // randomly select one of the two supporter queue to signal for first pasanger
            // set both supporters count values to zero 
            mutexSem.P();
                numManInTaxi.set(0);
                numLivInTaxi.set(0);
            mutexSem.V();
            // signal either queue randomly
            Random rand = new Random();
            int r = rand.nextInt(2);
            if (r == 0) {
                manQueueSem.V();
            } else {
                livQueueSem.V();
            }
            taxiFullSem.P(); // wait for the taxi cab to fill up with 4 supporters
            activities.printActivities();// display the passangers in the taxi cab
            CDS.idleQuietly((int) (Math.random() * 100)); //taking some time to travel to football ground       
            activities.removeAllActivities(); // all supporters vacate the taxi cab  
            CDS.idleQuietly((int) (Math.random() * 100)); // taxi cab returns for more passangers 
            if (trip < NUM_TRIPS-1)
                System.out.println("*** TAXI RETURNING FOR ANOTHER FARE ***");
            else
                System.out.println("**** TAXI RETURNING TO TAXI DEPOT *****");
        } // end for
    } // end run

} // Taxi Cab Process


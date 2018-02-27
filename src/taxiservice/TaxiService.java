/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package taxiservice;
import java.util.concurrent.atomic.*;

class TaxiService {
    // This SEMAPHORE based program simulates the  ferrying of  ManU and Liverpool
    // football supporters via a single taxi cab from the city centre to the football 
    // ground for the match.
    // There taxi cab cannot contain 3 ManU and 1 Liverpool fan (or vice versa), but
    // the taxi must be full (i.e. four passangers) before it leaves for the match. 

    // CONSTANTS to indicate the total number of supporters planning to travel
    static final int NUM_MAN_SUPPORTERS = 50;
    static final int NUM_LIV_SUPPORTERS = 50;
    // semaphore for the two supporters queues
    static MageeSemaphore manQueueSem, livQueueSem;
    // global variable for the number of each supporter in taxi
    static AtomicInteger numManInTaxi, numLivInTaxi;
    // semaphore to signal the taxi
    static MageeSemaphore taxiFullSem;
    // semaphore to provide mutual exclusion to global variables
    static MageeSemaphore mutexSem;
    // to identify a supporter
    static String nameOfSupporter;
    // reference to utility class for recording activity
    static Activity taxiActivities;

    public static void main(String[] args) {

        System.out.println("STARTED");

        // records all the supporter activity taking place
        taxiActivities = new Activity();

        // records the number each supporter in the taxi
        numManInTaxi = new AtomicInteger(0);
        numLivInTaxi = new AtomicInteger(0);

        // initialise the semaphore for ManU and Liverpool queues
        manQueueSem = new MageeSemaphore(0);
        livQueueSem = new MageeSemaphore(0);

        // initialise the semaphore for the taxi
        taxiFullSem = new MageeSemaphore(0);

        // initially access to global variables is available
        mutexSem = new MageeSemaphore(1);

        // create arrays to hold the football supporter processes from each club
        ManSupporterProcess[] manSupporterProcess = new ManSupporterProcess[NUM_MAN_SUPPORTERS];
        LivSupporterProcess[] livSupporterProcess = new LivSupporterProcess[NUM_LIV_SUPPORTERS];

        // create each ManU supporter process in the array of supporters
        for (int i = 0; i < NUM_MAN_SUPPORTERS; i++) {
            manSupporterProcess[i] = new ManSupporterProcess(manQueueSem, livQueueSem,
                    numManInTaxi, numLivInTaxi,
                    taxiFullSem, mutexSem,
                    "Man(" + i + ")", taxiActivities);
        }

        // create each Liverpool supporter process in the array of supporters
        for (int i = 0; i < NUM_LIV_SUPPORTERS; i++) {
            livSupporterProcess[i] = new LivSupporterProcess(manQueueSem, livQueueSem,
                    numManInTaxi, numLivInTaxi,
                    taxiFullSem, mutexSem,
                    "Liv(" + i + ")", taxiActivities);
        }

        //create taxi cab process
        TaxiCabProcess taxiCabProcess = new TaxiCabProcess(manQueueSem, livQueueSem,
                numManInTaxi, numLivInTaxi,
                taxiFullSem, mutexSem, taxiActivities);

        // set the ManU Supporter  processes running
        for (int i = 0; i < NUM_MAN_SUPPORTERS; i++) {
            CDS.idleQuietly((int) (Math.random() * 10));
            manSupporterProcess[i].start();
        }

        // set the Liverpool Supporter  processes running
        for (int i = 0; i < NUM_LIV_SUPPORTERS; i++) {
            CDS.idleQuietly((int) (Math.random() * 10));
            livSupporterProcess[i].start();
        }

        // set the taxi process running
        taxiCabProcess.start();

        // TAXI SERVICE RUNNING NOW!!
        
        // wait for the taxi to complete it work schedule
        try {
            taxiCabProcess.join();
        } catch (InterruptedException ex) {
            System.out.println(" Taxi never completed its work schedule");
        }

        // stop any ManU supporters threads that are still waiting after the taxi stops running
        for (int i = 0; i < NUM_MAN_SUPPORTERS; i++) {
            manSupporterProcess[i].stopThread();
        }

        // stop any Liverpool supporters threads that are still waiting after the taxi stops running
        for (int i = 0; i < NUM_LIV_SUPPORTERS; i++) {
            livSupporterProcess[i].stopThread();
        }

        // Final message
        System.out.println("The taxi completed its schedule of work ");
        //  System.exit(0);
    } // end main    

} // Taxi Service  



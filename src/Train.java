import TSim.*;
import java.util.concurrent.Semaphore;

public class Train implements Runnable {
    Lab1 parent;
    int id;
    int startpos;
    TSimInterface tsim;
    int speed;
    Boolean holds_prio, going_up;

    /*
     * startpos is 0 for the lowest rail, 1 for the second
     * lowest, 2 for the second highest and 3 for the highest rail.
     */
    public Train(Lab1 parent, int id, int startpos, int speed) {
        this.parent = parent;
        this.id = id;
        this.tsim = parent.tsim;
        this.speed = speed;

        if(id == 1) {
            going_up = false;
        }
        else {
            going_up = true;
        }

        // Claim semaphore for starting position
        if(startpos == 1) {
            // Startpos 1 is one of the shorter rails
            // Therefore it has a semaphore (startpos 0 is fallback)
            parent.sem[0].acquireUninterruptibly();
            holds_prio = true;
        }
        else if(startpos == 2) {
            // Startpos 2 is one of the shorter rails
            // Therefore it has a semaphore (startpos 3 is fallback)
            parent.sem[4].acquireUninterruptibly();
            holds_prio = true;
        }
        else {
            holds_prio = false;
        }
    }

    public void run() {
        try {
            // Start train
            tsim.setSpeed(id, speed);

            // The train runs until it meets a sensor.
            while(true) {
                SensorEvent sensor = tsim.getSensor(id);
                // When it comes across a new sensor it acts based on
                // which one it is

                // Ignore deactivation of sensors
                if(sensor.getStatus() == sensor.ACTIVE) {
                    int x = sensor.getXpos();
                    int y = sensor.getYpos();

                    // Station sensors, to prevent derailment.
                    if(
                       (x == 13 && (y == 13 || y == 11) && !going_up ) || // Upper station
                       ( x == 13 && (y == 5 || y == 3) && going_up ) ){// Lower station
                        // No semaphore changes, reverse train direction.
                        speed = -speed;
                        going_up = !going_up;
                        tsim.setSpeed(id, 0);
                        Thread.sleep(1000 + (20 * Math.abs(speed)));
                        tsim.setSpeed(id, speed);
                    }

                    // For each non-station sensor pair, depending
                    // on going up or down, one sem will be
                    // aquired and one released

                    // The switches above the crossing
                    else if( ( x == 6 && y == 6 ) || ( x == 9 && y == 5 )) {
                        if( !going_up ) { // If heading down
                            // Claim sem 5 (the crossing)
                            parent.claimSem(id, 5, speed);
                        }
                        else { // If heading up
                            // Release sem 5 (the crossing)
                            parent.releaseSem(id, 5);
                        }
                    }

                    // On the upside of top switch
                    else if( x == 13 && (y == 7 || y == 8) ) {
                        if( !going_up ) { // If heading down
                            // Release sem 5 (the crossing)
                            parent.releaseSem(id, 5);

                            // Sem handling automated by parent
                            parent.claimSem(id, 3, speed);

                            // Set switches to enter sem 3
                            if( y == 7 ) {
                                tsim.setSwitch(17, 7, 0);
                            }
                            else {
                                tsim.setSwitch(17, 7, 1);
                            }
                        }
                        else {
                            // Release sem 3
                            parent.releaseSem(id, 3);

                            // Claim sem 5
                            parent.claimSem(id, 5, speed);
                        }
                    }

                    // The right switch
                    else if( x == 19 && y == 9 ) {
                        if( !going_up ) { // headed down
                            // Release sem 4 (if held)
                            if( holds_prio ) {
                                parent.releaseSem(id, 4);
                            }
                            // Claim sem 2 if available
                            if( parent.tryClaimSem(id, 2) ) { // if sem 2 was free
                                // set switch to top rail
                                tsim.setSwitch(15, 9, 0);
                                holds_prio = true;
                            }
                            else {
                                // set switch to bottom rail
                                tsim.setSwitch(15, 9, 1);

                                // Make note of not holding the semaphore
                                holds_prio = false;
                            }
                        }
                        else { // headed up
                            // Release sem 2 (if held)
                            if( holds_prio ) {
                                parent.releaseSem(id, 2);
                            }
                            // Claim sem 4 if available
                            if( parent.tryClaimSem(id, 4) ) {
                                // Set switch to bottom rail
                                tsim.setSwitch(17, 7, 1);

                                // Make note of holding the semaphore
                                holds_prio = true;
                            }
                            else {
                                // set switch to top rail
                                tsim.setSwitch(17, 7, 0);

                                // Make note of not holding the semaphore
                                holds_prio = false;
                            }
                        }
                    }

                    // The middle right switches
                    else if( ( x == 12 && y == 9 ) ||
                             ( x == 13 && y == 10 ) ) {
                        if( !going_up ) { // Headed down
                            // Release sem 3
                            parent.releaseSem(id, 3);
                        }
                        else { // Headed up
                            // Claim sem 3
                            parent.claimSem(id, 3, speed);

                            // Set switch to enter sem 3
                            if( y == 10) {
                                tsim.setSwitch(15, 9, 1);
                            }
                            else {
                                tsim.setSwitch(15, 9, 0);
                            }
                        }
                    }

                    else if( ( x == 7 && y == 9 ) ||
                             ( x == 6 && y == 10 ) ){
                        if( !going_up ) { // headed down
                            // Claim sem 1
                            parent.claimSem(id, 1, speed);

                            // Set switch to enter sem 1
                            if( y == 10 ) {
                                tsim.setSwitch(4, 9, 0);
                            }
                            else {
                                tsim.setSwitch(4, 9, 1);
                            }
                        }
                        else {
                            // Release sem 1
                            parent.releaseSem(id, 1);
                        }
                    }

                    // Left sensor
                    else if( x == 1 && y == 10 ) {
                        if( !going_up ) { // Heading down
                            // Release sem 2, if held
                            if( holds_prio ) {
                                parent.releaseSem(id, 2);
                            }
                            // Claim sem 0 if available
                            if( parent.tryClaimSem(id, 0) ) { // if sem 0 was free
                                // set switch to top rail
                                tsim.setSwitch(3, 11, 1);
                                holds_prio = true;
                            }
                            else {
                                // set switch to bottom rail
                                tsim.setSwitch(3, 11, 0);

                                // Make note of not holding the semaphore
                                holds_prio = false;
                            }
                        }
                        else { // Heading up
                            // Release sem 0, if held
                            if( holds_prio ) {
                                parent.releaseSem(id, 0);
                            }
                            // Claim sem 2 if available
                            if( parent.tryClaimSem(id, 2) ) { // if sem 0 was free
                                // set switch to top rail
                                tsim.setSwitch(4, 9, 1);
                                holds_prio = true;
                            }
                            else {
                                // set switch to bottom rail
                                tsim.setSwitch(4, 9, 0);

                                // Make note of not holding the semaphore
                                holds_prio = false;
                            }
                        }

                    }

                    // The sensors on the inside of the bottom switch
                    else if( ( x == 6 && y == 11 ) ||
                             ( x == 4 && y == 13 ) ) {
                        if( !going_up ) { // Heading down
                            // Release sem 1
                            parent.releaseSem(id, 1);
                        }
                        else { // Heading up
                            // Claim sem 1
                            parent.claimSem(id, 1, speed);

                            // Set switch to enter 1 safely
                            if( y == 11 ) {
                                tsim.setSwitch(3, 11, 1);
                            }
                            else {
                                tsim.setSwitch(3, 11, 0);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();    // or only e.getMessage() for the error
            System.exit(1);
        }
    }
}

import TSim.*;
import java.util.concurrent.Semaphore;

public class Train implements Runnable {
    Lab1 parent;
    int id;
    int startpos;
    TSimInterface tsim;
    int speed;
    Boolean holds_prio;

    /*
     * startpos is 0 for the uppermost rail, 1 for the second
     * uppermost, 2 for the second lowest and 3 for the lowest rail.
     */
    public Train(Lab1 parent, int id, int startpos, int speed) {
        this.parent = parent;
        this.id = id;
        this.tsim = parent.tsim;
        this.speed = speed;
        this.speed = -1;

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
                       (x == 13 && (y == 13 || y == 11) && speed < 0 ) // Upper station
                       ||
                       ( x == 13 && (y == 5 || y == 3) && speed > 0 ) // Lower station
                       ){
                        // No semaphore changes, reverse train direction.
                        speed = -speed;
                        Thread.sleep(2000 - (100 * Math.abs(speed)));
                        tsim.setSpeed(id, 0);
                        Thread.sleep(1000 + (20 * Math.abs(speed)));
                        tsim.setSpeed(id, speed);
                    }

                    // For each non-station sensor pair, depending
                    // on going up or down, one sem will be
                    // aquired and one released

                    // The switches above the crossing
                    if( (x == 6 || x == 9) && y == 5 ) {
                        if( speed < 0 ) { // If heading down
                            // Claim sem 5 (the crossing)
                            parent.claimSem(id, 5, speed);
                        }
                        else { // If heading up
                            // Release sem 5 (the crossing)
                            parent.releaseSem(id, 5);
                        }
                    }

                    // On the inside of top switch
                    if( x == 13 && (y == 7 || y == 8) ) {
                        if( speed < 0 ) { // If heading down
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
                            // Release sem 3 (if held)
                            if ( holds_prio ) {
                                parent.releaseSem(id, 3);
                            }

                            // Claim sem 5
                            parent.claimSem(id, 5, speed);
                        }
                    }

                    // The right switch
                    if( x == 19 && y == 9 ) {
                        if( speed < 0 ) { // headed down
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
                            }
                        }
                        else { // headed up
                            // Release sem 2 (if held)
                            if( holds_prio ) {
                                parent.releaseSem(id, 2);
                            }
                            // Claim sem 4 if available
                            if( parent.tryClaimSem(id, 2) ) {
                                // Set switch to bottom rail

                                // note that you are holding the sem
                                holds_prio = true;
                            }
                            else {
                                // set switch to top rail
                            }
                        }
                    }

                    // // On the inside of left switch, prevents derailment
                    // if( x == 5 && y == 9 ) {
                    //     tsim.setSwitch(4, 9, 1);
                    // }
                    // if( x == 4 && y == 10 ) {
                    //     tsim.setSwitch(4, 9, 0);
                    // }

                    // // On the inside of right switch, prevents derailment
                    // if( x == 14 && y == 9) {
                    //     tsim.setSwitch(15, 9, 1);
                    // }
                    // if( x == 15 && y == 10) {
                    //     tsim.setSwitch(15, 9, 0);
                    // }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();    // or only e.getMessage() for the error
            System.exit(1);
        }
    }
}

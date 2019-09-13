import TSim.*;

public class Train implements Runnable {
    Lab1 parent;
    int id;
    int startpos;
    TSimInterface tsim;
    int speed;

    /*
     * startpos is 0 for the uppermost rail, 1 for the second
     * uppermost, 2 for the second lowest and 3 for the lowest rail.
     */
    public Train(Lab1 parent, int id, int startpos, int speed) {
        this.parent = parent;
        this.id = id;
        this.tsim = parent.tsim;
        this.speed = speed;

        // Claim semaphore for starting position
        if(startpos == 1) {
            // Startpos 1 is one of the shorter rails
            // Therefore it has a semafore (startpos 0 is fallback)

            // Claim semaphore here, TODO
        }
        if(startpos == 2) {
            // Startpos 2 is one of the shorter rails
            // Therefore it has a semafore (startpos 3 is fallback)

            // Claim semaphore here, TODO
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
                    if( x == 16 && (y == 13 || y == 11 || y == 5 || y == 3)) {
                        // No semaphore changes, reverse train direction.
                        speed = -speed;
                        tsim.setSpeed(id, 0);
                        Thread.sleep(2000);
                        tsim.setSpeed(id, speed);
                    }

                    // Group by heading up or down
                    //(up is positive speed, i flipped a train.)
                    if(speed < 0) { // Heading down
                        // On the inside of top switch, prevents derailment
                        if( x == 16 && y == 7 ) {
                            tsim.setSwitch(17, 7, 0);
                        }
                        if( x == 17 && y == 8 ) {
                            tsim.setSwitch(17, 7, 1);
                        }

                        // On the inside of left switch, prevents derailment
                        if( x == 5 && y == 9 ) {
                            tsim.setSwitch(4, 9, 1);
                        }
                        if( x == 4 && y == 10 ) {
                            tsim.setSwitch(4, 9, 0);
                        }
                    }
                    else { // Heading up
                        // On the inside of right switch, prevents derailment
                        if( x == 14 && y == 9) {
                            tsim.setSwitch(15, 9, 1);
                        }
                        if( x == 15 && y == 10) {
                            tsim.setSwitch(15, 9, 0);
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

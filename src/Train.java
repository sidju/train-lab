import TSim.*;

public class Train implements Runnable {
    Lab1 parent;
    int id;
    Boolean going_up;
    TSimInterface tsim;
    int speed;

    public Train(Lab1 parent, int id, Boolean going_up, int speed) {
        this.parent = parent;
        this.id = id;
        this.tsim = parent.tsim;
        this.going_up = going_up;
        this.speed = speed;
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

                    // Bottom station sensors, to prevent derailment.
                    if( x == 16 && (y == 13 || y == 11 || y == 5 || y == 3)) {
                        // No semaphore changes, reverse train direction.
                        speed = -speed;
                        tsim.setSpeed(id, 0);
                        Thread.sleep(2000);
                        tsim.setSpeed(id, speed);
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

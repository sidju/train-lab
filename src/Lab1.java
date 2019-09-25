import TSim.*;
import java.util.concurrent.Semaphore;

public class Lab1 {
    TSimInterface tsim;
    Semaphore[] sem;

    public Lab1(int speed1, int speed2) {
        tsim = TSimInterface.getInstance();
        sem = new Semaphore[6];
        for(int i = 0; i < sem.length; i++) {
            sem[i] = new Semaphore(1);
        }

        Train one = new Train(this, 1, 3, speed1);
        Thread t1 = new Thread(one);
        Train two = new Train(this, 2, 1, speed2);
        Thread t2 = new Thread(two);

        t1.start();
        t2.start();
    }

    public boolean tryClaimSem(int train, int sem) {
        if( this.sem[sem].tryAcquire(1) ) {
            System.out.printf("Train %d took sem %d.\n", train, sem);
            return true;
        }
        else {
            System.out.printf("Train %d tried but couldn't take sem %d.\n", train, sem);
            return false;
        }
    }

    public void claimSem(int train, int sem, int speed) {
        if( ! this.sem[sem].tryAcquire(1) ) {
            try {
                tsim.setSpeed(train, 0);
                this.sem[sem].acquireUninterruptibly(1);
                tsim.setSpeed(train, speed);
            } catch (Exception e) {
                e.printStackTrace();    // or only e.getMessage() for the error
                System.exit(1);
            }
        }
        System.out.printf("Train %d took sem %d.\n", train, sem);
    }

    public void releaseSem(int train, int sem) {
        this.sem[sem].release();
        System.out.printf("Train %d released sem %d\n", train, sem);
    }
}

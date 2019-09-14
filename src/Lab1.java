import TSim.*;
import java.util.concurrent.locks.ReentrantLock;

public class Lab1 {
    TSimInterface tsim;
    ReentrantLock[] lock;

    public Lab1(int speed1, int speed2) {
        tsim = TSimInterface.getInstance();
        lock = new ReentrantLock[6];
        for(int i = 0; i < lock.length; i++) {
            lock[i] = new ReentrantLock();
        }

        // Since train 2 will not be created yet
        lock[0].lock();

        Train one = new Train(this, 1, 0, speed1);
        Thread t1 = new Thread(one);

        t1.run();
    }

    public boolean tryClaimLock(int train, int lock) {
        if( this.lock[lock].tryLock() ) {
            System.out.printf("Train %d took lock %d.\n", train, lock);
            return true;
        }
        else {
            System.out.printf("Train %d tried but couldn't take lock %d.\n", train, lock);
            return false;
        }
    }

    public void claimLock(int train, int lock, int speed) {
        if( ! this.lock[lock].tryLock() ) {
            try {
                tsim.setSpeed(train, 0);
                this.lock[lock].lock();
                tsim.setSpeed(train, speed);
            } catch (Exception e) {
                e.printStackTrace();    // or only e.getMessage() for the error
                System.exit(1);
            }
        }
        System.out.printf("Train %d took lock %d.\n", train, lock);
    }

    public void tryReleaseLock(int train, int lock) {
        if( this.lock[lock].isHeldByCurrentThread() ) {
            this.lock[lock].unlock();
            System.out.printf("Train %d released lock %d\n", train, lock);
        }
        else {
            System.out.printf("Train %d tried to release lock %d, which it didn't own.\n", train, lock);
            //System.exit(1);
        }
    }
}

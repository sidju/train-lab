import TSim.*;

public class Lab1 {
    TSimInterface tsim;

    public Lab1(int speed1, int speed2) {
        tsim = TSimInterface.getInstance();
        Train one = new Train(this, 1, true, speed1);
        Thread t1 = new Thread(one);

        t1.run();
    }
}

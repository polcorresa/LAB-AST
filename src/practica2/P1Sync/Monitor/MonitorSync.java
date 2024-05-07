package practica2.P1Sync.Monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorSync {

    private final int N;
    private int id;
    
    protected Lock mon = new ReentrantLock();
    protected Condition next = mon.newCondition();

    public MonitorSync(int N) {
        this.N = N;
    }

    public void waitForTurn(int id) {
        mon.lock();
        while(this.id!=id){
            next.awaitUninterruptibly();
        }
        mon.unlock();
    }

    public void transferTurn() {
        mon.lock();
        id = (id+1)%N;
        next.signalAll();
        mon.unlock();
    }
}

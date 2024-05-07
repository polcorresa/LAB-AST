package practica2.P1Sync;

import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CounterThreadID extends Thread {
    private final int id;
    protected Lock control = new ReentrantLock();
    protected Condition cond = control.newCondition();

    public CounterThreadID(int id) {
        this.id = id;
        
        
    }
    
    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            cond.signal();
            System.out.print(id);
            
            
        }
    }
}

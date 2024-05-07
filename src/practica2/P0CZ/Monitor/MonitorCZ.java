package practica2.P0CZ.Monitor;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class MonitorCZ{

    private int x = 0;
    protected Lock control;
    
    public MonitorCZ(){
        control = new ReentrantLock();
    }

    public void inc() {
        control.lock();
        try {
            //Incrementa en una unitat el valor d'x
            x = x+1;
        } finally {
            control.unlock();
        }
    }

    public int getX() {
        int val;
        control.lock();
        try {        
            //Ha de retornar el valor d'x
            val = x;
        } finally {        
            control.unlock();
        }
        
        
        return val;
    }

}

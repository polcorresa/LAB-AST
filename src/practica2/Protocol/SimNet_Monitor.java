package practica2.Protocol;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.SimNet;

public class SimNet_Monitor implements SimNet {

    protected CircularQueue<TCPSegment> queue;
    protected Lock mon = new ReentrantLock();
    protected Condition plena = mon.newCondition();
    protected Condition buida = mon.newCondition();

    public SimNet_Monitor() {
        queue = new CircularQueue<>(Const.SIMNET_QUEUE_SIZE);

    }

    @Override
    public void send(TCPSegment seg) {
        try {
            mon.lock();
            while (queue.full()) {
                plena.awaitUninterruptibly();
            }
            queue.put(seg);
            buida.signal();
        } finally {
            mon.unlock();
        }
    }

    @Override
    public TCPSegment receive() {
        try {
            mon.lock();
            while (queue.empty()) {
                buida.awaitUninterruptibly();
            }
            TCPSegment seg = queue.get();
            plena.signal();
            return seg;
        } finally {
            mon.unlock();
        }

    }

    @Override
    public int getMTU() {
        return Const.MTU_ETHERNET;
    }

}

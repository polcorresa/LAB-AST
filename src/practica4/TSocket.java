package practica4;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

    //sender variable:
    protected int MSS;

    //receiver variables:
    protected CircularQueue<TCPSegment> rcvQueue;
    protected int rcvSegConsumedBytes;

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        MSS = network.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        rcvQueue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        rcvSegConsumedBytes = 0;
    }

    @Override
    public void sendData(byte[] data, int offset, int length) {
        int altLength;
        while (length > 0) {
            altLength = Math.min(MSS, length);
            TCPSegment segment = segmentize(data, offset, altLength);
            segment.setPsh(true);
            segment.setSourcePort(localPort);
            segment.setDestinationPort(remotePort);
            network.send(segment);

            this.printSndSeg(segment);
            offset += altLength;
            length -= altLength;
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        TCPSegment segment = new TCPSegment();
        segment.setData(data, offset, length);
        return segment;
    }

    @Override
    public int receiveData(byte[] buf, int offset, int length) {
        lock.lock();
        try {
            int consumidos = 0;
            while (rcvQueue.empty()) {
                appCV.awaitUninterruptibly();
            }

            while (consumidos < length && !rcvQueue.empty()) {
                consumidos += consumeSegment(buf, offset + consumidos, length - consumidos);
            }
            sendAck();
            return consumidos;
        } finally {
            lock.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
        TCPSegment seg = rcvQueue.peekFirst();
        int a_agafar = Math.min(length, seg.getDataLength() - rcvSegConsumedBytes);
        System.arraycopy(seg.getData(), rcvSegConsumedBytes, buf, offset, a_agafar);
        rcvSegConsumedBytes += a_agafar;
        if (rcvSegConsumedBytes == seg.getDataLength()) {
            rcvQueue.get();
            rcvSegConsumedBytes = 0;
        }
        return a_agafar;
    }

    protected void sendAck() {
        TCPSegment seg = new TCPSegment();
        seg.setAck(true);
        seg.setDestinationPort(remotePort);
        seg.setSourcePort(localPort);
        network.send(seg);

    }

    @Override
    public void processReceivedSegment(TCPSegment rseg) {

        lock.lock();
        try {
            if (rseg.isAck()) {
                //nothing to be done in this exercise.
            }
            if (!rcvQueue.full()) {
                rcvQueue.put(rseg);
                this.printRcvSeg(rseg);
            
                appCV.signal();
            }
        } finally {
            lock.unlock();
        }
    }

}

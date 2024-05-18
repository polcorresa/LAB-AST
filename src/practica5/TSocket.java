package practica5;

import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TSocket_base;
import util.TCPSegment;

public class TSocket extends TSocket_base {

    // Sender variables:
    protected int MSS;
    protected int snd_sndNxt;
    protected int snd_rcvWnd;
    protected int snd_rcvNxt;
    protected TCPSegment snd_UnacknowledgedSeg;
    protected boolean zero_wnd_probe_ON;

    // Receiver variables:
    protected CircularQueue<TCPSegment> rcv_Queue;
    protected int rcv_SegConsumedBytes;
    protected int rcv_rcvNxt;

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        // init sender variables
        MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        // init receiver variables
        //rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        rcv_Queue = new CircularQueue<>(2);
        snd_rcvWnd = Const.RCV_QUEUE_SIZE;
    }

    // -------------  SENDER PART  ---------------
    @Override
    public void sendData(byte[] data, int offset, int length) {
        lock.lock();
        try {
            int altLength;
            while (length > 0) {
                if (zero_wnd_probe_ON) {
                    altLength = 1;
                } else {
                    altLength = Math.min(MSS, length);
                }
                TCPSegment segment = segmentize(data, offset, altLength);

                snd_UnacknowledgedSeg = segment;
                network.send(segment);
                this.startRTO();
                this.printSndSeg(segment);
                snd_sndNxt++;
                offset += altLength;
                length -= altLength;
                while (snd_rcvNxt != snd_sndNxt) {
                    appCV.awaitUninterruptibly();
                }
            }

        } finally {
            lock.unlock();
        }
    }

    protected TCPSegment segmentize(byte[] data, int offset, int length) {
        TCPSegment segment = new TCPSegment();
        segment.setPsh(true);
        segment.setSourcePort(localPort);
        segment.setDestinationPort(remotePort);
        segment.setData(data, offset, length);
        segment.setSeqNum(snd_sndNxt);

        return segment;
    }

    @Override
    protected void timeout() {
        lock.lock();
        try {
            if (snd_UnacknowledgedSeg != null) {
                if (zero_wnd_probe_ON) {
                    log.printPURPLE("0-wnd probe: " + snd_UnacknowledgedSeg);
                } else {
                    log.printPURPLE("retrans: " + snd_UnacknowledgedSeg);
                }
                network.send(snd_UnacknowledgedSeg);
                startRTO();
            }
        } finally {
            lock.unlock();
        }
    }

    // -------------  RECEIVER PART  ---------------
    @Override
    public int receiveData(byte[] buf, int offset, int maxlen) {
        lock.lock();
        try {
            int consumidos = 0;
            while (rcv_Queue.empty()) {
                appCV.awaitUninterruptibly();
            }

            while (consumidos < maxlen && !rcv_Queue.empty()) {
                consumidos += consumeSegment(buf, offset + consumidos, maxlen - consumidos);
            }

            return consumidos;
        } finally {
            lock.unlock();
        }
    }

    protected int consumeSegment(byte[] buf, int offset, int length) {
        TCPSegment seg = rcv_Queue.peekFirst();
        int a_agafar = Math.min(length, seg.getDataLength() - rcv_SegConsumedBytes);
        System.arraycopy(seg.getData(), rcv_SegConsumedBytes, buf, offset, a_agafar);
        rcv_SegConsumedBytes += a_agafar;
        if (rcv_SegConsumedBytes == seg.getDataLength()) {
            rcv_Queue.get();
            rcv_SegConsumedBytes = 0;
        }
        return a_agafar;
    }

    protected void sendAck() {
        TCPSegment seg = new TCPSegment();
        seg.setAck(true);
        seg.setDestinationPort(remotePort);
        seg.setSourcePort(localPort);
        seg.setAckNum(rcv_rcvNxt);
        seg.setWnd(rcv_Queue.free());
        this.printSndSeg(seg);
        network.send(seg);

    }

    // -------------  SEGMENT ARRIVAL  -------------
    @Override
    public void processReceivedSegment(TCPSegment rseg) {
        lock.lock();
        try {
            if (rseg.isAck()) {
                this.stopRTO();
                snd_rcvNxt = rseg.getAckNum();
                snd_rcvWnd = rseg.getWnd();
                if (snd_rcvWnd == 0) {
                    zero_wnd_probe_ON = true;
                    System.out.println("---zero_wnd_probe ON");
                } else {
                    zero_wnd_probe_ON = false;
                    System.out.println("---zero_wnd_probe OFF");
                }
                this.printRcvSeg(rseg);
                appCV.signalAll();

            }
            if (!rcv_Queue.full() && rseg.isPsh() && rseg.getSeqNum() == rcv_rcvNxt) {
                rcv_Queue.put(rseg);
                this.printRcvSeg(rseg);
                rcv_rcvNxt++;
                sendAck();

                appCV.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}

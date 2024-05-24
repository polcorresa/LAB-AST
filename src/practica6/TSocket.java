package practica6;

import java.util.Iterator;
import practica1.CircularQ.CircularQueue;
import practica4.Protocol;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

public class TSocket extends TSocket_base {

    // Sender variables:
    protected int MSS;
    protected int snd_sndNxt;
    protected int snd_rcvNxt;
    protected int snd_rcvWnd;
    protected int snd_cngWnd;
    protected int snd_minWnd;
    protected CircularQueue<TCPSegment> snd_unacknowledged_segs;
    protected boolean zero_wnd_probe_ON;

    // Receiver variables:
    protected int rcv_rcvNxt;
    protected CircularQueue<TCPSegment> rcv_Queue;
    protected int rcv_SegConsumedBytes;

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        this.localPort = localPort;
        this.remotePort = remotePort;
        p.addActiveTSocket(this);
        // init sender variables:
        MSS = p.getNetwork().getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
        MSS = 10;
        // init receiver variables:
        rcv_Queue = new CircularQueue<>(Const.RCV_QUEUE_SIZE);
        snd_rcvWnd = Const.RCV_QUEUE_SIZE;
        snd_cngWnd = 3;
        snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
        snd_unacknowledged_segs = new CircularQueue(snd_cngWnd);
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

                snd_unacknowledged_segs.put(segment);
                network.send(segment);
                this.startRTO();
                this.printSndSeg(segment);
                snd_sndNxt++;
                offset += altLength;
                length -= altLength;
                while (snd_sndNxt - snd_rcvNxt >= snd_minWnd) {
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
            if (!snd_unacknowledged_segs.empty()) {
                if (zero_wnd_probe_ON) {
                    log.printPURPLE("0-wnd probe: " + snd_unacknowledged_segs.peekFirst());
                } else {
                    log.printPURPLE("retrans: " + snd_unacknowledged_segs.peekFirst());
                }
                for (TCPSegment x : snd_unacknowledged_segs) {
                    network.send(x);
                    startRTO();
                }

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

        network.send(seg);
        //this.printSndSeg(seg);
    }

    // -------------  SEGMENT ARRIVAL  -------------
    public void processReceivedSegment(TCPSegment rseg) {

        lock.lock();
        try {
            if (rseg.isAck()) {
                this.stopRTO();
                while (!snd_unacknowledged_segs.empty() && rseg.getAckNum() > snd_unacknowledged_segs.peekFirst().getSeqNum()) {
                    System.out.println("Locked here");
                    System.out.println(snd_unacknowledged_segs.peekFirst().getSeqNum() + "ackNum: " + rseg.getAckNum());
                    snd_unacknowledged_segs.get();
                }
                snd_rcvNxt = rseg.getAckNum();
                snd_rcvWnd = rseg.getWnd();
                snd_minWnd = Math.min(snd_rcvWnd, snd_cngWnd);
                if (snd_minWnd == 0) {
                    zero_wnd_probe_ON = true;
                    snd_minWnd = 1;
                    System.out.println("---zero_wnd_probe ON---");
                } else {
                    if (zero_wnd_probe_ON) {
                        System.out.println("---zero_wnd_probe OFF---");
                    }
                    zero_wnd_probe_ON = false;

                }
                this.printRcvSeg(rseg);
                appCV.signalAll();
                this.startRTO();

            }
            if (!rcv_Queue.full() && rseg.isPsh()) {
                if (rseg.getSeqNum() == rcv_rcvNxt) {
                    rcv_Queue.put(rseg);
                    this.printRcvSeg(rseg);
                    rcv_rcvNxt++;
                }
                sendAck();

                appCV.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    private void unacknowledgedSegments_content() {
        Iterator<TCPSegment> ite = snd_unacknowledged_segs.iterator();
        log.printBLACK("\n-------------- content begins  --------------");
        while (ite.hasNext()) {
            log.printBLACK(ite.next().toString());
        }
        log.printBLACK("-------------- content ends    --------------\n");
    }
}

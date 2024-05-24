package practica7;

import practica1.CircularQ.CircularQueue;
import util.Const;
import util.TCPSegment;
import util.TSocket_base;

/**
 * Connection oriented Protocol Control Block.
 *
 * Each instance of TSocket maintains all the status of an endpoint.
 *
 * Interface for application layer defines methods for passive/active opening
 * and for closing the connection. Interface lower layer defines methods for
 * processing of received segments and for sending of segments. We assume an
 * ideal lower layer with no losses and no errors in packets.
 *
 * State diagram:
 * <pre>
 * +---------+
 * |  CLOSED |-------------
 * +---------+             \
 * LISTEN  |                   \
 * ------  |                    | CONNECT
 * V                    | -------
 * +---------+               | snd SYN
 * |  LISTEN |               |
 * +---------+          +----------+
 * |               | SYN_SENT |
 * |               +----------+
 * rcv SYN   |                    |
 * -------   |                    | rcv SYN
 * snd SYN   |                    | -------
 * |                    |
 * V                   /
 * +---------+             /
 * |  ESTAB  |<------------
 * +---------+
 * CLOSE    |     |    rcv FIN
 * -------   |     |    -------
 * +---------+          snd FIN  /       \                    +---------+
 * |  FIN    |<-----------------           ------------------>|  CLOSE  |
 * |  WAIT   |------------------           -------------------|  WAIT   |
 * +---------+          rcv FIN  \       /   CLOSE            +---------+
 * -------   |      |  -------
 * |      |  snd FIN
 * V      V
 * +----------+
 * |  CLOSED  |
 * +----------+
 * </pre>
 *
 * @author AST's teachers
 */
public class TSocket extends TSocket_base {

    protected Protocol proto;

    protected int state;
    protected CircularQueue<TSocket> acceptQueue;

    // States of FSM:
    protected final static int CLOSED = 0,
            LISTEN = 1,
            SYN_SENT = 2,
            ESTABLISHED = 3,
            FIN_WAIT = 4,
            CLOSE_WAIT = 5;

    protected TSocket(Protocol p, int localPort, int remotePort) {
        super(p.getNetwork());
        proto = p;
        this.localPort = localPort;
        this.remotePort = remotePort;
        state = CLOSED;
        p.addActiveTSocket(this);
    }

    @Override
    public void connect() {
        lock.lock();
        try {
            proto.addActiveTSocket(this);

            TCPSegment seg = new TCPSegment();
            seg.setSyn(true);
            seg.setSourcePort(localPort);
            seg.setDestinationPort(remotePort);

            network.send(seg);
            this.printSndSeg(seg);
            this.state = SYN_SENT;
            this.appCV.signalAll();

            while (this.state != ESTABLISHED) {
                appCV.awaitUninterruptibly();
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            TCPSegment segFin = new TCPSegment();
            segFin.setFin(true);
            segFin.setDestinationPort(remotePort);
            segFin.setSourcePort(localPort);
            network.send(segFin);
            this.printSndSeg(segFin);
            
            switch (state) {
                case ESTABLISHED:
                    System.out.println("---FINWAIT---");
                    this.state = FIN_WAIT;
//                    while(state != CLOSED){
//                        appCV.awaitUninterruptibly();
//                    }
                    break;
                case CLOSE_WAIT: {
                    System.out.println("---CLOSED---");
                    this.state = CLOSED;
                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Segment arrival.
     *
     */
    @Override
    public void processReceivedSegment(TCPSegment rseg) {
        lock.lock();
        try {

            printRcvSeg(rseg);

            switch (state) {

                case SYN_SENT: {
                    if (rseg.isSyn()) {
                        System.out.println("---ESTABLISHED---");
                        this.state = ESTABLISHED;
                        appCV.signalAll();
                    }
                    break;
                }

                case ESTABLISHED:    
                case FIN_WAIT:
                case CLOSE_WAIT: {
                    if (rseg.isPsh()) {
                        if (state == ESTABLISHED || state == FIN_WAIT) {
                            // Here should go the segment's data processing.
                        } else {
                            // This should not occur, since a FIN has been 
                            // received from the remote side. 
                            // Ignore the data segment.
                        }
                    }
                    if (rseg.isFin()) {
                        switch(state){
                            case ESTABLISHED: {
                                System.out.println("---CLOSE WAIT---");
                                state = CLOSE_WAIT;
                                close();
                                break;
                            }
                            case FIN_WAIT:{
                                System.out.println("---CLOSED---");
                                state = CLOSED;
                                appCV.signalAll();
                            }
                        }
                    }

                    break;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    protected void printRcvSeg(TCPSegment rseg) {
        log.printBLACK("    rcvd: " + rseg);
    }

    protected void printSndSeg(TCPSegment rseg) {
        log.printBLACK("    sent: " + rseg);
    }

}

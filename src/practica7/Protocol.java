package practica7;

import util.Protocol_base;
import util.TCPSegment;
import util.SimNet;
import util.TSocket_base;

public class Protocol extends Protocol_base {

    protected Protocol(SimNet network) {
        super(network);
    }

    public void ipInput(TCPSegment segment) {
        TSocket_base base = getMatchingTSocket(segment.getDestinationPort(), segment.getSourcePort());
        if (base != null) {
            base.processReceivedSegment(segment);
        }
    }

    protected TSocket_base getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            for (TSocket_base x : activeSockets) {
                if (x.localPort == localPort && x.remotePort == remotePort) {
                    return x;
                }
            }
            for(TSocket_base y: listenSockets){
                if (y.localPort == localPort) {
                    return y;
                }
            }
            return null;

        } finally {
            lk.unlock();
        }
    }

}

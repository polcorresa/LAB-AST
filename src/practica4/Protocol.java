package practica4;

import util.Protocol_base;
import util.TCPSegment;
import util.SimNet;
import util.TSocket_base;

public class Protocol extends Protocol_base {

    public Protocol(SimNet network) {
        super(network);
    }

    protected void ipInput(TCPSegment seg) {
         TSocket_base base = getMatchingTSocket(seg.getDestinationPort(), seg.getSourcePort());
         if(base != null){
             base.processReceivedSegment(seg);
         }
    }

    protected TSocket_base getMatchingTSocket(int localPort, int remotePort) {
        lk.lock();
        try {
            int i = 0;
            for (TSocket_base x : activeSockets) {
                if (x.localPort == localPort && x.remotePort == remotePort) {
                    return x;
                }
            }
            return null;

        } finally {
            lk.unlock();
        }

    }
}

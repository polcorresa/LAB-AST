package practica3;

import util.Const;
import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketSend extends TSocket_base {

    protected int MSS;       // Maximum Segment Size

    public TSocketSend(SimNet network) {
        super(network);
        MSS = network.getMTU() - Const.IP_HEADER - Const.TCP_HEADER;
    }

    @Override
    public void sendData(byte[] data, int offset, int length) {
        
        
        int altLength;
        while (length > 0) {
            altLength = Math.min(MSS, length);
            TCPSegment segment = segmentize(data, offset, altLength);
            segment.setPsh(true);
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

}

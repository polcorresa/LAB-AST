package practica1.Protocol;

import util.TCPSegment;
import util.TSocket_base;
import util.SimNet;

public class TSocketRecv extends TSocket_base {

  public TSocketRecv(SimNet network) {
    super(network);
  }

  @Override
  public int receiveData(byte[] data, int offset, int length) {
    TCPSegment segment = this.network.receive();
    byte[] rData = segment.getData();
    for(int i = 0; i < segment.getDataLength(); i++){
        data[i+offset]= rData[i];
    }
    this.printRcvSeg(segment);
    return segment.getDataLength();
  }
}


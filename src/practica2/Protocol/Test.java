package practica2.Protocol;

import practica1.Protocol.SimNet_Queue;
import practica1.Protocol.TSocketRecv;
import practica1.Protocol.TSocketSend;
import util.Receiver;
import util.Sender;
import util.TCPSegment;
import util.SimNet;

public class Test {

  public static void main(String[] args) throws InterruptedException {
    
    TCPSegment.SHOW_DATA = true;
    
    SimNet net        = new SimNet_Monitor();
    Sender sender     = new Sender(new TSocketSend(net), 200, 1, 100);
    Receiver receiver = new Receiver(new TSocketRecv(net), 1, 2000);

    //Completar (trobar una manera que demostri que la xarxa utilitzada no funciona bé per aquest cas)
    
    receiver.start();
    
    sender.start();

  }
}

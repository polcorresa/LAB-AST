package practica3;

import practica2.Protocol.SimNet_Monitor;
import util.Receiver;
import util.Sender;
import util.SimNet;

public class Test {

    public static void main(String[] args) {
        SimNet net = new SimNet_Loss(0.5);
        new Sender(new TSocketSend(net), 5, 3000, 100).start();
        new Receiver(new TSocketRecv(net), 2000, 10).start();
    }
}

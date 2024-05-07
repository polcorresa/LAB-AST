package practica2.P0CZ.Monitor;

public class TestSumCZ {

    public static void main(String[] args) throws InterruptedException {
        
           MonitorCZ mon = new MonitorCZ();
           CounterThreadCZ thread1 = new CounterThreadCZ(mon);
           CounterThreadCZ thread2 = new CounterThreadCZ(mon);
           
           thread1.start();
           thread2.start();
           int i = -1;
           i = i%10;
           System.out.println(i);
           
    }
}

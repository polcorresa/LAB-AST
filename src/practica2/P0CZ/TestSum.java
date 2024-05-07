package practica2.P0CZ;

public class TestSum {

    public static void main(String[] args) throws InterruptedException {
        CounterThread thread1 = new CounterThread();
        CounterThread thread2 = new CounterThread();
        
        thread1.start();
        thread2.start(); /* Cada vez que ejecutamos el codigo recibimos un valor distinto de x, 
        cuando siempre deberia de ser 20000. Esto es debido a que las instrucciones de los dos threads se "superponen"*/
        
        
    }
}

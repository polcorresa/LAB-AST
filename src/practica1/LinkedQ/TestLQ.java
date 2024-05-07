package practica1.LinkedQ;

import java.util.Arrays;

public class TestLQ {

  public static void main(String[] args) {
    LinkedQueue<Integer> q = new LinkedQueue<>();
    for (int i = 0; i<5;i++){
    
    q.put(i);
    
    }
    for (int i = 10; i<14;i++){
    q.put(i);
    }
  }
}

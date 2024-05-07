package practica1.CircularQ;

import java.util.Iterator;
import util.Queue;

public class CircularQueue<E> implements Queue<E> {

    private final E[] queue;
    private final int N;
    private int n, G;

    public CircularQueue(int N) {
        this.N = N;
        queue = (E[]) (new Object[N]);
    }

    @Override
    public int size() {
        return this.N;
    }

    @Override
    public int free() {
        return this.N-this.n;
    }

    @Override
    public boolean empty() {
        if (n == 0) return true;
        return false;
    }

    @Override
    public boolean full() {
        if (n == N)
            return true;
        return false;
    }
    @Override
    public E peekFirst() {
        if (this.empty()) return null;
        return this.queue[G];
    }

    @Override
    public E get() {
        if (this.empty()) return null;
        E e = this.queue[G];
        G = (G+1)%N;
        n--;
        return e;        
    }

    @Override
    public void put(E e) {
        if(!this.full()){
        this.queue[(G+n)%N] = e;
        n++;
        }
        else {
        System.out.println("La cola está llena.");
        }
    }

    @Override
    public String toString() {
        if(this.empty()){return "";}
        int i;
        String string = "";
        
        for(i = 0; i< n;i++){
            
            string = string + this.queue[(G+i)%n].toString();
            string = string + ", ";
        }
        return string;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator {

        private int pos = G-1;
        private boolean first = true;
               
        @Override
        public boolean hasNext() {
            if (((pos+1)%N == (G+n)%N)&&(!first)){
     
                return false;
            }
            first = false;
            return true;
        }

        @Override
        public E next() {
                pos = (pos+1)%N;
                E e = queue[pos];
                return e;
        }

        @Override
        public void remove() {
           int i;
            n--;
            for(i = pos; i<G+n; i++){
               queue[i%N] = queue [(i+1)%N];
           }
            pos--;
        }

    }
}

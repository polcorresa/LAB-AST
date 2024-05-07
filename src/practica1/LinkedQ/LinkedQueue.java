package practica1.LinkedQ;

import java.util.Iterator;
import util.Queue;

public class LinkedQueue<E> implements Queue<E> {

    private int n;
    private Node<E> primer, ultim;

    @Override
    public int size() {
        return this.n;
    }

    @Override
    public boolean empty() {
        if (n == 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean full() {
        return false;
    }

    @Override
    public int free() {
        return 0;
    }

    @Override
    public E peekFirst() {
        return this.primer.getValue();
    }

    @Override
    public E get() {
        E e = this.primer.getValue();
        this.primer = this.primer.getNext();
        n--;
        if (n == 0) {
            this.ultim = null;
        }
        return e;
    }

    @Override
    public void put(E e) {
        Node nou = new Node();
        nou.setValue(e);
        if (n == 0) {
            this.primer = nou;
        } else {
            this.ultim.setNext(nou);
        }
        this.ultim = nou;
        n++;
    }

    @Override
    public String toString() {
        String string = "";

        Node index = primer;
        for (int i = 0; i < n; i++) {
            string = string + index.getValue().toString();
            string = string + ", ";
            index = index.getNext();
        }
        return string;
    }

    @Override
    public Iterator<E> iterator() {
        return new MyIterator();
    }

    class MyIterator implements Iterator {

        private int pos;

        @Override
        public boolean hasNext() {
            if (pos < n) {
                return true;
            }
            return false;
        }

        @Override
        public E next() {
            Node element = primer;
            for (int j = 0; j < pos; j++) {
                element = element.getNext();
            }
            E ret = (E) element.getValue();
            pos++;
            return ret;
        }

        @Override
        public void remove() {
            Node elementP = primer;
            if (pos == 1) {
                primer = primer.getNext();
            } else {

                for (int j = 0; j < pos - 2; j++) {
                    elementP = elementP.getNext();
                }

                Node elementD = elementP.getNext();
                elementD = elementD.getNext();
                
                if (pos == n) {
                    elementP.setNext(ultim);
                } else {
                    elementP.setNext(elementD);
                }
            }
            n--;
            if (n == 0) {
                ultim = null;
            }
            pos--;
        }

    }
}

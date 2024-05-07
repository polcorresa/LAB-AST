package practica1.LinkedQ;

public class Node<X> {

  private X value;
  private Node next;

  public X getValue() {
    return value;
  }

  public void setValue(X value) {
    this.value = value;
  }

  public Node getNext() {
    return next;
  }

  public void setNext(Node next) {
    this.next = next;
  }

}

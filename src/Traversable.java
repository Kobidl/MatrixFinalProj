import java.util.Collection;

/**
 * This interface defines the functionality required for a traversable graph
 */
public interface Traversable<T> {
    void addBanIndex(T index);

    public Node<T> getOrigin();
    public int getValue(T index);

    public Collection<Node<T>>  getReachableNodes(Node<T> someNode,boolean includeDiagonal);
    public Collection<Node<T>>  getNeighborNodes(Node<T> someNode,boolean includeDiagonal);
}
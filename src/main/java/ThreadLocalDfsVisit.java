import java.util.*;

public class ThreadLocalDfsVisit<T> {
    protected final ThreadLocal<Stack<Node<T>>> stackThreadLocal =
            ThreadLocal.withInitial(Stack::new);
    protected final ThreadLocal<Set<Node<T>>> setThreadLocal =
            ThreadLocal.withInitial(()->new HashSet<>());


    protected void threadLocalPush(Node<T> node){
        stackThreadLocal.get().push(node);
    }

    protected Node<T> threadLocalPop(){
        return stackThreadLocal.get().pop();
    }


    /**
     * Get all linked points from Traversable source
     * @param partOfGraph Traversable
     * @param includeDiagonal boolean - if should include diagonals
     * @return HashSet of T
     */
    public HashSet<T> traverse(Traversable<T> partOfGraph,boolean includeDiagonal){
        //push starting point to stack
        threadLocalPush(partOfGraph.getOrigin());

        while(!stackThreadLocal.get().isEmpty()){
            //pop Node from stack
            Node<T> poppedNode = threadLocalPop();
            //save Node in visited set
            setThreadLocal.get().add(poppedNode);
            //find reachable nodes from popped Node
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(poppedNode,includeDiagonal);
            for (Node<T> singleReachableNode: reachableNodes){
                //Check if first time see this Node
                //if it does add to stack
                if (!setThreadLocal.get().contains(singleReachableNode) &&
                        !stackThreadLocal.get().contains(singleReachableNode)){
                    threadLocalPush(singleReachableNode);
                }
            }
        }

        //return all the Nodes that were visited
        HashSet<T> blackList = new HashSet<>();
        for (Node<T> node: setThreadLocal.get()){
            blackList.add(node.getData());
        }
        return blackList;
    }

    /**
     * Reset the stack and set
     */
    public void reset() {
        stackThreadLocal.get().clear();
        setThreadLocal.get().clear();
    }
}
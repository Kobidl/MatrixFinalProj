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


    public List<T> traverse(Traversable<T> partOfGraph,boolean includeDiagonal){
        threadLocalPush(partOfGraph.getOrigin());
        while(!stackThreadLocal.get().isEmpty()){
            Node<T> poppedNode = threadLocalPop();
            setThreadLocal.get().add(poppedNode);
            Collection<Node<T>> reachableNodes = partOfGraph.getReachableNodes(poppedNode,includeDiagonal);
            for (Node<T> singleReachableNode: reachableNodes){
                if (!setThreadLocal.get().contains(singleReachableNode) &&
                        !stackThreadLocal.get().contains(singleReachableNode)){
                    threadLocalPush(singleReachableNode);
                }
            }
        }
        List<T> blackList = new ArrayList<>();
        for (Node<T> node: setThreadLocal.get()){
            blackList.add(node.getData());
        }
        return blackList;
    }

}